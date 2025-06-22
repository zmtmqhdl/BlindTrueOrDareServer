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

val roomSessions = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

fun Application.webSocketRoute() {
    routing {
        webSocket("/game") {
            val params = call.request.queryParameters
            val roomId = params["roomId"]
            val userId = params["userId"]

            if (roomId == null || userId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing parameters"))
                return@webSocket
            }

            println("🔌 WebSocket 연결됨: user=$userId, room=$roomId")

            // 세션 등록
            val sessions = roomSessions.getOrPut(roomId) { mutableSetOf() }
            sessions += this

            send("✅ WebSocket 연결 완료: $userId")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val msg = frame.readText()
                        println("💬 [$userId] 메시지: $msg")

                        // 같은 방의 다른 사용자에게 브로드캐스트
                        sessions.forEach { session ->
                            if (session != this) {
                                session.send("[$userId]: $msg")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("⚠️ WebSocket 에러: $e")
            } finally {
                println("🔌 연결 종료됨: $userId")
                sessions -= this
            }
        }
    }
}