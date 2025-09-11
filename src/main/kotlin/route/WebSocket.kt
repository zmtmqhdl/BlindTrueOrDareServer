package org.example.route

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.mapper.toDomain
import org.example.mapper.toDto
import org.example.model.*

val roomSessions = mutableMapOf<String, RoomData>()

fun Application.webSocketRoute() {
    routing {
        webSocket("/room") {
            val params = call.request.queryParameters
            val roomId = params["roomId"]

            val playerJson = params["player"]
            val player: Player? = playerJson?.let {
                try {
                    Json.decodeFromString<PlayerDto>(it).toDomain()
                } catch (e: Exception) {
                    application.log.error("❌ Failed to decode Player: ${e.message}", e)
                    null
                }
            }

            if (roomId == null || player == null) {
                application.log.warn("⚠️ WebSocket connection failed - Missing parameters: roomId=$roomId, player=$player")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            val currentRoom = roomSessions.getOrPut(roomId) {
                application.log.info("🟢 Creating new room: $roomId by host=${player.nickname}")
                RoomData(
                    room = Room(
                        roomId = roomId,
                        host = player,
                        participantList = mutableSetOf(),
                        roomStatus = RoomStatus.WAIT,
                        writeTime = 0L,
                        questionNumber = 0,
                        questionList = mutableListOf()
                    ),
                    sessions = mutableMapOf(),
                    writeCompletePlayerList = mutableSetOf(),
                    answerCompletePlayerList = mutableSetOf(),
                )
            }

            currentRoom.sessions[player.playerId] = this
            application.log.info("🔌 Player connected: ${player.nickname} (${player.playerId}) to room=$roomId")

            if (!currentRoom.room.participantList.map { it.playerId }.contains(player.playerId)) {
                currentRoom.room.participantList.add(player)
                application.log.info("👥 Player joined room=$roomId: ${player.nickname}")
                updateMessage(
                    sessions = currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                    room = currentRoom.room
                )
            }

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val messageJson = frame.readText()
                        try {
                            val message = Json.decodeFromString<MessageDto>(messageJson).toDomain()
                            application.log.debug("📩 Received message from ${player.nickname}: ${message.type}")

                            when (message.type) {
                                MessageType.SEND_START -> {
                                    if (message.player?.playerId == currentRoom.room.host.playerId) {
                                        currentRoom.room.roomStatus = RoomStatus.WRITE
                                        currentRoom.room.writeTime = 30L
                                        currentRoom.room.questionNumber = 1
                                        application.log.info("🚀 Game started in room=$roomId by host=${player.nickname}")
                                        updateMessage(
                                            sessions = currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                                            room = currentRoom.room
                                        )
                                    }
                                }

                                MessageType.SEND_WRITE_END -> {
                                    if (!currentRoom.writeCompletePlayerList.contains(message.player!!)) {
                                        currentRoom.writeCompletePlayerList.add(message.player)
                                        application.log.info("✍️ Player finished writing: ${player.nickname}")

                                        val questionList = Json.decodeFromString<List<QuestionDto>>(message.data!!)
                                            .map { it.toDomain() }
                                        val newQuestionList = questionList.mapIndexed { index, question ->
                                            question.copy(questionId = (currentRoom.room.questionList.size + index + 1).toLong())
                                        }
                                        currentRoom.room.questionList.addAll(newQuestionList.filter { it.question.isNotEmpty() })
                                    }

                                    if (currentRoom.writeCompletePlayerList.size == currentRoom.room.participantList.size) {
                                        currentRoom.room.questionList.shuffle()
                                        currentRoom.room.roomStatus =
                                            if (currentRoom.room.questionList.isEmpty()) RoomStatus.RESULT else RoomStatus.ANSWER
                                        application.log.info("✅ All players finished writing in room=$roomId → Next phase: ${currentRoom.room.roomStatus}")
                                        updateMessage(
                                            currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                                            currentRoom.room
                                        )
                                    }
                                }

                                MessageType.SEND_ANSWER_END -> {
                                    if (!currentRoom.answerCompletePlayerList.contains(message.player!!)) {
                                        currentRoom.answerCompletePlayerList.add(message.player)
                                        application.log.info("🗳️ Player answered: ${player.nickname}")

                                        val answerList = Json.decodeFromString<List<AnswerDto>>(message.data!!)
                                            .map { it.toDomain() }
                                        answerList.forEach { answer ->
                                            val index = currentRoom.room.questionList.indexOfFirst { it.questionId == answer.questionId }
                                            if (answer.answer == true) {
                                                currentRoom.room.questionList[index].oVoter.add(answer.player)
                                            } else if (answer.answer == false) {
                                                currentRoom.room.questionList[index].xVoter.add(answer.player)
                                            } else {
                                                currentRoom.room.questionList[index].xVoter.add(answer.player)
                                            }
                                        }
                                    }

                                    if (currentRoom.answerCompletePlayerList.size == currentRoom.room.participantList.size) {
                                        currentRoom.room.roomStatus = RoomStatus.RESULT
                                        application.log.info("📊 All players finished answering in room=$roomId → RESULT phase")
                                        updateMessage(
                                            sessions = currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                                            room = currentRoom.room
                                        )
                                        currentRoom.room.participantList = mutableSetOf()
                                        currentRoom.room.roomStatus = RoomStatus.WAIT
                                    }
                                }

                                MessageType.REJOIN -> {
                                    application.log.info("🔄 Player rejoined: ${player.nickname} in room=$roomId")



                                    // 2. 새 세션 등록 (먼저 교체)
                                    currentRoom.room.participantList.add(player)

                                    // 3. 전체 유저에게 UPDATE 브로드캐스트
                                    updateMessage(
                                        sessions = currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                                        room = currentRoom.room
                                    )
                                }


                                else -> {
                                    application.log.debug("ℹ️ Ignored message type=${message.type} from ${player.nickname}")
                                }
                            }
                        } catch (e: Exception) {
                            application.log.error("❌ Failed to process incoming message: ${e.message}", e)
                        }
                    }
                }
            } catch (e: CancellationException) {
                application.log.info("🔄 Session cancelled for ${player.nickname} in room=$roomId (reason=${e.message})")
            } catch (e: Exception) {
                application.log.error("⚠️ WebSocket error: ${e.message}", e)
            } finally {
                currentRoom.sessions.remove(player.playerId)
                currentRoom.room.participantList.remove(player)
                application.log.info("🔴 Player disconnected: ${player.nickname} from room=$roomId")

                if (player == currentRoom.room.host && currentRoom.room.participantList.isNotEmpty()) {
                    currentRoom.room.host = currentRoom.room.participantList.first()
                    application.log.info("👑 Host left, new host=${currentRoom.room.host.nickname} in room=$roomId")
                }

                if (currentRoom.sessions.isEmpty()) {
                    application.log.info("🗑️ Room removed: $roomId (no active sessions)")
                    roomSessions.remove(roomId)
                } else {
                    updateMessage(
                        sessions = currentRoom.room.participantList.mapNotNull { currentRoom.sessions[it.playerId] },
                        room = currentRoom.room
                    )
                }
            }
        }
    }
}

suspend fun updateMessage(sessions: Collection<DefaultWebSocketServerSession>, room: Room) {
    val message = Message(
        type = MessageType.UPDATE,
        data = Json.encodeToString(room.toDto()),
        timestamp = System.currentTimeMillis()
    ).toDto()
    sessions.forEach { it.send(Json.encodeToString(message)) }
}
