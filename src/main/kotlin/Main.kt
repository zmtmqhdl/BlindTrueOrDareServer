package org.example

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.example.databaseFactory.connectToDatabase
import org.example.route.waitingRoom
import org.example.route.webSocketRoute
import java.time.Duration
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    connectToDatabase()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(30)
        }

        routing {
            waitingRoom()

//            webSocket("/game") {
//                send("🟢 BlindTrueOrDare 서버에 연결되었습니다.")
//
//                for (frame in incoming) {
//                    if (frame is Frame.Text) {
//                        val receivedText = frame.readText()
//                        println("📨 클라이언트 메시지: $receivedText")
//
//                        // Echo 응답
//                        send("서버 응답: '$receivedText' 잘 받았어!")
//                    }
//                }
//            }
        }

        webSocketRoute()


    }.start(wait = true)
}