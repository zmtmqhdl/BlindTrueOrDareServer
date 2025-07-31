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
                    application.log.error("❌ Player 디코딩 실패: ${e.message}", e)
                    null
                }
            }

            if (roomId == null || player == null) {
                application.log.warn("WebSocket 연결 실패 - 파라미터 누락: waitingRoomId=$roomId, player=$player")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            val currentRoom = roomSessions.getOrPut(roomId) {
                RoomData(
                    room = Room(
                        roomId = roomId,
                        hostId = player.playerId,
                        participantList = mutableSetOf(),
                        roomStatus = RoomStatus.WAIT,
                        writeTime = 0L,
                        questionNumber = 0,
                        questionList = mutableListOf()
                    ),
                    sessions = mutableSetOf(),
                    writeCompletePlayerList = mutableSetOf(),
                    answerCompletePlayer = mutableSetOf(),
                )
            }
            application.log.info("대기실 데이터: $currentRoom")
            currentRoom.sessions += this

            // 새로운 유저 입장
            if (!currentRoom.room.participantList.map { it.playerId }.contains(player.playerId)) {
                currentRoom.room.participantList.add(player)
                application.log.info("플레이어 추가됨: ${player.playerId} -> 방 ID: $roomId")

                updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
            }
            application.log.info("✅ WebSocket 연결 완료: ${player.playerId}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val messageJson = frame.readText()
                        try {
                            val message = Json.decodeFromString<MessageDto>(messageJson).toDomain()

                            when (message.type) {
                                MessageType.SEND_START -> {
                                    if (message.playerId == currentRoom.room.hostId) {
                                        application.log.info("▶️ 시작 메시지 수신")
                                        currentRoom.room.roomStatus = RoomStatus.WRITE
                                        currentRoom.room.writeTime = 3000L
                                        currentRoom.room.questionNumber = 3
                                        updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                                    }
                                }
                                MessageType.SEND_WRITE_END -> {
                                    application.log.info("▶️ 질문 작성 완료 메시지 수신 ${message.playerId}")
                                    if (!currentRoom.writeCompletePlayerList.contains(element = message.playerId!!)) {
                                        currentRoom.writeCompletePlayerList.add(element = message.playerId)
                                        val questionList = Json.decodeFromString<List<QuestionDto>>(message.data!!).map { it.toDomain() }
                                        val newQuestionList = questionList.mapIndexed { index, question ->
                                            question.copy(questionId = (currentRoom.room.questionList.size + index + 1).toLong())
                                        }
                                        currentRoom.room.questionList.addAll(newQuestionList)
                                        application.log.info("▶️ 질문 목록 ${currentRoom.room.questionList}")
                                    }
                                    if (currentRoom.writeCompletePlayerList.size == currentRoom.sessions.size) {
                                        currentRoom.room.questionList.shuffle()
                                        currentRoom.room.roomStatus = RoomStatus.ANSWER
                                        updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
                                    }
                                }
                                MessageType.SEND_ANSWER_END -> {
                                    application.log.info("▶️ 답변 완료 메시지 수신 ${message.playerId}")
                                    if (!currentRoom.answerCompletePlayer.contains(element = message.playerId!!)) {
                                        currentRoom.answerCompletePlayer.add(element = message.playerId)
                                        val answerList =
                                            Json.decodeFromString<List<AnswerDto>>(message.data!!).map { it.toDomain() }
                                        answerList.forEach { answer ->
                                            val index = currentRoom.room.questionList.indexOfFirst { it.questionId == answer.questionId }
                                            if (answer.answer) {
                                                currentRoom.room.questionList[index].oVoters.add(element = answer.playerId)
                                            } else {
                                                currentRoom.room.questionList[index].xVoters.add(element = answer.playerId)
                                            }
                                        }
                                        application.log.info("▶️ 질문 목록 ${currentRoom.room.questionList}")
                                    }
                                    if (currentRoom.answerCompletePlayer.size == currentRoom.sessions.size) {
                                        currentRoom.room.roomStatus = RoomStatus.END
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
                application.log.error("⚠️ WebSocket 에러: ${e.message}", e)
            } finally {
                application.log.info("🔌 연결 종료됨: ${player.playerId}")
                currentRoom.sessions -= this
                currentRoom.room.participantList.remove(player)
                updateMessage(sessions = currentRoom.sessions, room = currentRoom.room)
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