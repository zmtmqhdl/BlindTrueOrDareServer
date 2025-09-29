package org.example

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.example.route.room
import org.example.route.webSocketRoute
import java.time.Duration
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
//    connectToDatabase()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(30)
        }

        routing {
            room()
        }

        webSocketRoute()


    }.start(wait = true)
}