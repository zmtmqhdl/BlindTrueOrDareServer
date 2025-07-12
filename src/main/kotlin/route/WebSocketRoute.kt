package org.example.route

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.json.Json
import org.example.mapper.toDomain
import org.example.model.Player
import org.example.model.PlayerDto
import org.example.model.WaitingRoom
import org.example.model.WaitingRoomData
import org.example.model.WaitingRoomStatus

val roomSessions = mutableMapOf<String,WaitingRoomData>()

fun Application.webSocketRoute() {
    routing {
        webSocket("/createWaitingRoom") {
            val params = call.request.queryParameters
            val waitingRoomId = params["waitingRoomId"]

            val playerJson = params["player"]
            val player: Player? = playerJson?.let {
                try {
                    Json.decodeFromString<PlayerDto>(it).toDomain()
                } catch (e: Exception) {
                    println("❌ Player 디코딩 실패: ${e.message}")
                    null
                }
            }

            if (waitingRoomId == null || player == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            val roomData = roomSessions.getOrPut(waitingRoomId) {
                WaitingRoomData(
                    waitingRoom = WaitingRoom(
                        roomId = waitingRoomId,
                        hostId = player.playerId,
                        participantList = mutableListOf(),
                        status = WaitingRoomStatus.Waiting
                    ),
                    sessions = mutableSetOf()
                )
            }
            println("$roomData")

            if (!roomData.waitingRoom.participantList.map { it.playerId}.contains(player.playerId)) {
                roomData.waitingRoom.participantList.add(player)
            }
            roomData.sessions += this




            send("✅ WebSocket 연결 완료: $player.playerId")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val msg = frame.readText()
                        println("💬 [$player.playerId] 메시지: $msg")

                        // 같은 방의 다른 사용자에게 브로드캐스트
                        roomData.sessions.forEach { session ->
                            if (session != this) {
                                session.send("[$player.playerId]: $msg")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("⚠️ WebSocket 에러: $e")
            } finally {
                println("🔌 연결 종료됨: $player.playerId")
                roomData.sessions -= this
            }
        }
    }
}