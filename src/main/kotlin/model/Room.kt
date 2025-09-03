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
    var host: Player,
    val participantList: MutableSet<Player>,
    var roomStatus: RoomStatus,
    var writeTime: Long,
    var questionNumber: Long,
    var questionList: MutableList<Question>
)

@Serializable
data class RoomDto(
    val roomId: String,
    val host: PlayerDto,
    val participantList: Set<PlayerDto>,
    val roomStatus: RoomStatus,
    val writeTime: Long,
    val questionNumber: Long,
    val questionList: List<QuestionDto>
)

data class Question(
    var questionId: Long,
    val playerId: String,
    val question: String,
    val oVoters: MutableSet<String>,
    val xVoters: MutableSet<String>
)

@Serializable
data class QuestionDto(
    val questionId: Long,
    val playerId: String,
    val question: String,
    val oVoters: Set<String>,
    val xVoters: Set<String>
)

data class Answer(
    val questionId: Long,
    val playerId: String,
    val answer: Boolean?
)

@Serializable
data class AnswerDto(
    val questionId: Long,
    val playerId: String,
    val answer: Boolean?
)

data class RoomData(
    val room: Room,
    var sessions: MutableSet<DefaultWebSocketServerSession>,
    var writeCompletePlayerList: MutableSet<String>,
    var answerCompletePlayer: MutableSet<String>,
)



enum class RoomStatus {
    WAIT,
    READY,
    WRITE,
    ANSWER,
    RESULT
}
