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
data class CreateWaitingRoomRequest(
    val player: Player
)

@Serializable
data class CreateWaitingRoomResponse(
    val waitingRoomId: String
)


data class WaitingRoom(
    val waitingRoomId: String,
    val hostId: String,
    val participantList: MutableList<Player>,
    val waitingRoomStatus: WaitingRoomStatus
)

@Serializable
data class WaitingRoomDto(
    val waitingRoomId: String,
    val hostId: String,
    val participantList: List<PlayerDto>,
    val waitingRoomStatus: WaitingRoomStatus
)

data class WaitingRoomData(
    val waitingRoom: WaitingRoom,
    var sessions: MutableSet<DefaultWebSocketServerSession>
)

enum class WaitingRoomStatus {
    Waiting,
    Playing
}