package org.example.route

import com.example.presentation.util.IdGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.model.CreateRoomRequest
import org.example.model.CreateRoomResponse

val roomStorage = mutableMapOf<String, CreateRoomRequest>()

// 뭔가 이상하다? 왜? 저걸 하는가?
fun Route.room() {
    route("/room") {
        post("/create") {
            try {
                val request = call.receive<CreateRoomRequest>()
                val roomId = IdGenerator()
                roomStorage[roomId] = request
                call.respond(HttpStatusCode.Created, CreateRoomResponse(
                    roomId = roomId
                    )
                )
            } catch (e: Exception) {
                call.respondText("Bad Request: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}