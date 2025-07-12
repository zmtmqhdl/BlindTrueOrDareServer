package org.example.route

import com.example.presentation.util.IdGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.model.CreateWaitingRoomRequest
import org.example.model.CreateWaitingRoomResponse

val roomStorage = mutableMapOf<String, CreateWaitingRoomRequest>()

fun Route.waitingRoom() {
    route("/waitingRoom") {
        post("/create") {
            try {
                val request = call.receive<CreateWaitingRoomRequest>()
                val waitingRoomId = IdGenerator()
                roomStorage[waitingRoomId] = request
                call.respond(HttpStatusCode.Created, CreateWaitingRoomResponse(waitingRoomId))

            } catch (e: Exception) {
                call.respondText("Bad Request: ${e.message}", status = io.ktor.http.HttpStatusCode.BadRequest)
            }
        }
    }
}