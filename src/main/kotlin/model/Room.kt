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
    val player: Player,
    val question: String,
    val oVoter: MutableSet<Player>,
    val xVoter: MutableSet<Player>,
    val noAnswer: MutableSet<Player>
)

@Serializable
data class QuestionDto(
    val questionId: Long,
    val player: PlayerDto,
    val question: String,
    val oVoter: Set<PlayerDto>,
    val xVoter: Set<PlayerDto>,
    val noAnswer: Set<PlayerDto>
)

data class Answer(
    val questionId: Long,
    val player: Player,
    val answer: Boolean?
)

@Serializable
data class AnswerDto(
    val questionId: Long,
    val player: PlayerDto,
    val answer: Boolean?
)

data class RoomData(
    val room: Room,
    var sessions: MutableSet<DefaultWebSocketServerSession>,
    var writeCompletePlayerList: MutableSet<Player>,
    var answerCompletePlayerList: MutableSet<Player>,
)



enum class RoomStatus {
    WAIT,
    WRITE,
    ANSWER,
    RESULT
}
