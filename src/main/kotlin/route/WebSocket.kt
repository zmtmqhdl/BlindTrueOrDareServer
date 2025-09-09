package org.example.route

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.*
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
                application.log.warn("WebSocket connection failed - Missing parameters: roomId=$roomId, player=$player")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            val currentRoom = roomSessions.getOrPut(roomId) {
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
                    sessions = mutableSetOf(),
                    writeCompletePlayerList = mutableSetOf(),
                    answerCompletePlayerList = mutableSetOf(),
                )
            }
            application.log.info("Room data: $currentRoom")
            currentRoom.sessions += this

            // New player joined
            if (!currentRoom.room.participantList.map { it.playerId }.contains(player.playerId)) {
                currentRoom.room.participantList.add(player)
                application.log.info("Player added: ${player.playerId} -> Room ID: $roomId")

                updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
            }
            application.log.info("✅ WebSocket connection established: ${player.playerId}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val messageJson = frame.readText()
                        try {
                            val message = Json.decodeFromString<MessageDto>(messageJson).toDomain()

                            when (message.type) {
                                MessageType.SEND_START -> {
                                    if (message.player?.playerId == currentRoom.room.host.playerId) {
                                        application.log.info("▶️ Start message received")
                                        currentRoom.room.roomStatus = RoomStatus.WRITE
                                        currentRoom.room.writeTime = 30L
                                        currentRoom.room.questionNumber = 3
                                        updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                                    }
                                }
                                MessageType.SEND_WRITE_END -> {
                                    application.log.info("▶️ Write completed message received ${message.player}")
                                    if (!currentRoom.writeCompletePlayerList.contains(element = message.player!!)) {
                                        currentRoom.writeCompletePlayerList.add(element = message.player)
                                        val questionList = Json.decodeFromString<List<QuestionDto>>(message.data!!).map { it.toDomain() }
                                        val newQuestionList = questionList.mapIndexed { index, question ->
                                            question.copy(questionId = (currentRoom.room.questionList.size + index + 1).toLong())
                                        }
                                        currentRoom.room.questionList.addAll(newQuestionList.filter { it.question.isNotEmpty() })
                                        application.log.info("▶️ Question list ${currentRoom.room.questionList}")
                                    }
                                    if (currentRoom.writeCompletePlayerList.size == currentRoom.room.participantList.size) {
                                        currentRoom.room.questionList.shuffle()
                                        currentRoom.room.roomStatus = RoomStatus.ANSWER
                                        updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                                    }
                                }
                                MessageType.SEND_ANSWER_END -> {
                                    application.log.info("▶️ Answer completed message received ${message.player}")
                                    if (!currentRoom.answerCompletePlayerList.contains(element = message.player!!)) {
                                        currentRoom.answerCompletePlayerList.add(element = message.player)
                                        val answerList =
                                            Json.decodeFromString<List<AnswerDto>>(message.data!!).map { it.toDomain() }
                                        answerList.forEach { answer ->
                                            val index = currentRoom.room.questionList.indexOfFirst { it.questionId == answer.questionId }
                                            if (answer.answer == true) {
                                                currentRoom.room.questionList[index].oVoter.add(element = answer.player)
                                            } else if (answer.answer == false) {
                                                currentRoom.room.questionList[index].xVoter.add(element = answer.player)
                                            } else {
                                                currentRoom.room.questionList[index].xVoter.add(element = answer.player)
                                            }
                                        }
                                        application.log.info("▶️ Question list ${currentRoom.room.questionList}")
                                    }
                                    if (currentRoom.answerCompletePlayerList.size == currentRoom.room.participantList.size) {
                                        currentRoom.room.roomStatus = RoomStatus.RESULT
                                        updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                                    }
                                }

                                else -> {

                                }
                            }
                        } catch (e: Exception) {

                        }
                    }
                }
            } catch (e: Exception) {
                application.log.error("⚠️ WebSocket error: ${e.message}", e)
            } finally {
                application.log.info("🔌 Disconnected: ${player.playerId}")
                currentRoom.sessions -= this
                currentRoom.room.participantList.remove(player)

                if (player == currentRoom.room.host && currentRoom.room.participantList.isNotEmpty()) {
                    currentRoom.room.host = currentRoom.room.participantList.first()
                    application.log.info("🔌 Host changed: ${currentRoom.room}")
                }

                if (currentRoom.sessions.isEmpty()) {
                    roomSessions.remove(roomId)
                    application.log.info("🗑️ Room removed: $roomId (no players left)")
                } else {
                    updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                }
            }
        }
    }
}

suspend fun updateMessage(sessions: Set<DefaultWebSocketServerSession>, room: Room) {
    val message = Message(
        type = MessageType.UPDATE,
        data = Json.encodeToString<RoomDto>(value = room.toDto()),
        timestamp = System.currentTimeMillis()
    ).toDto()
    sessions.forEach {
        it.send(Json.encodeToString(value = message))
    }
}
