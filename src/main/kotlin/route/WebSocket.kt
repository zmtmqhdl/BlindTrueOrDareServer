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
                        participantList = mutableListOf(),
                        roomStatus = RoomStatus.WAIT,
                        writeTime = 0L,
                        questionNumber = 0
                    ),
                    sessions = mutableSetOf()
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
                                    application.log.info("▶️ 게임 시작 요청 수신")
                                    application.log.info("${message.senderId}    ${currentRoom.room.hostId}")
                                    if (message.senderId == currentRoom.room.hostId) {
                                        currentRoom.room.roomStatus = RoomStatus.WRITE
                                        currentRoom.room.writeTime = 3600L
                                        currentRoom.room.questionNumber = 3
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