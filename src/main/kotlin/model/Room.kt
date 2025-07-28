package org.example.model

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val playerId: String,
    val nickname: String
)

@Serializable
data class PlayerDto(
    val playerId: String,
    val nickname: String
)

@Serializable
data class CreateRoomRequest(
    val player: Player
)

@Serializable
data class CreateRoomResponse(
    val roomId: String
)


data class Room(
    val roomId: String,
    val hostId: String,
    val participantList: MutableSet<Player>,
    var roomStatus: RoomStatus,
    var writeTime: Long,
    var questionNumber: Int
)

@Serializable
data class RoomDto(
    val roomId: String,
    val hostId: String,
    val participantList: MutableSet<PlayerDto>,
    val roomStatus: RoomStatus,
    val writeTime: Long,
    val questionNumber: Int
)

data class Question(
    val writerId: String,
    val question: String,
    val oVoters: MutableSet<String>,
    val xVoters: MutableSet<String>
)

data class RoomData(
    val room: Room,
    var sessions: MutableSet<DefaultWebSocketServerSession>,
    var writeCompletePlayer: MutableSet<String>,
    var answerCompletePlayer: MutableSet<String>,
    var questionList: MutableSet<Question>
)



enum class RoomStatus {
    WAIT,
    WRITE,
    ANSWER,
    END
}
