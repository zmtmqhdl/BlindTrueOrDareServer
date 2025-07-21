package org.example.route

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.mapper.toDomain
import org.example.mapper.toDto
import org.example.model.Message
import org.example.model.MessageDto
import org.example.model.MessageType
import org.example.model.Player
import org.example.model.PlayerDto
import org.example.model.WaitingRoom
import org.example.model.WaitingRoomData
import org.example.model.WaitingRoomDto
import org.example.model.WaitingRoomStatus

val roomSessions = mutableMapOf<String,WaitingRoomData>()

fun Application.webSocketRoute() {
    routing {
        webSocket("/waitingRoom") {
            val params = call.request.queryParameters
            val waitingRoomId = params["waitingRoomId"]

            val playerJson = params["player"]
            val player: Player? = playerJson?.let {
                try {
                    Json.decodeFromString<PlayerDto>(it).toDomain()
                } catch (e: Exception) {
                    application.log.error("❌ Player 디코딩 실패: ${e.message}", e)
                    null
                }
            }

            if (waitingRoomId == null || player == null) {
                application.log.warn("WebSocket 연결 실패 - 파라미터 누락: waitingRoomId=$waitingRoomId, player=$player")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            val currentWaitingRoom = roomSessions.getOrPut(waitingRoomId) {
                WaitingRoomData(
                    waitingRoom = WaitingRoom(
                        waitingRoomId = waitingRoomId,
                        hostId = player.playerId,
                        participantList = mutableListOf(),
                        waitingRoomStatus = WaitingRoomStatus.Waiting,
                    ),
                    sessions = mutableSetOf()
                )
            }
            application.log.info("대기실 데이터: $currentWaitingRoom")
            currentWaitingRoom.sessions += this

            // 새로운 유저 입장
            if (!currentWaitingRoom.waitingRoom.participantList.map { it.playerId }.contains(player.playerId)) {
                currentWaitingRoom.waitingRoom.participantList.add(player)
                application.log.info("플레이어 추가됨: ${player.playerId} -> 방 ID: $waitingRoomId")

                updateMessage(sessions = currentWaitingRoom.sessions, waitingRoom = currentWaitingRoom.waitingRoom)
            }

            application.log.info("✅ WebSocket 연결 완료: ${player.playerId}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val messageJson = frame.readText()
                        try {
                            val message = Json.decodeFromString<MessageDto>(messageJson)

                            when (message.type) {

                            }


                        } catch (e: Exception) {

                        }


                        // 같은 방의 다른 사용자에게 브로드캐스트
                        currentWaitingRoom.sessions.forEach { session ->
                            if (session != this) {
                                session.send("[${player.playerId}]: $messageJson")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                application.log.error("⚠️ WebSocket 에러: ${e.message}", e)
            } finally {
                application.log.info("🔌 연결 종료됨: ${player.playerId}")
                currentWaitingRoom.sessions -= this
                currentWaitingRoom.waitingRoom.participantList.remove(player)
                updateMessage(sessions = currentWaitingRoom.sessions, waitingRoom = currentWaitingRoom.waitingRoom)
            }
        }
    }
}

suspend fun updateMessage(sessions: Set<DefaultWebSocketServerSession>, waitingRoom: WaitingRoom) {
    val message = Message(
        type = MessageType.UPDATE,
        data = Json.encodeToString<WaitingRoomDto>(value = waitingRoom.toDto()),
        timestamp = System.currentTimeMillis()
    )
    sessions.forEach {
        it.send(Json.encodeToString(value = message))
    }
}