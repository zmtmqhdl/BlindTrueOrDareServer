package org.example.route

import com.example.presentation.util.idGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.model.CreateRoomRequest
import org.example.model.CreateRoomResponse
import java.util.concurrent.ConcurrentHashMap

val roomStorage = ConcurrentHashMap<String, CreateRoomRequest>()

fun Route.room() {
    route("/room") {
        post("/create") {
            try {
                val request = call.receive<CreateRoomRequest>()

                var roomId: String
                do {
                    roomId = idGenerator()
                } while (roomStorage.putIfAbsent(roomId, request) != null)

                call.respond(
                    HttpStatusCode.Created,
                    CreateRoomResponse(roomId = roomId)
                )
            } catch (e: Exception) {
                call.respondText("Bad Request: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}