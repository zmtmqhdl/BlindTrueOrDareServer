package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
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


@Serializable
data class WaitingRoom(
    val roomId: String,
    val hostId: String,
    val participantList: List<Player>,
    val status: WaitingRoomStatus
)

@Serializable
data class WaitingRoomDto(
    val roomId: String,
    val hostId: String,
    val participantList: List<Player>,
    val status: WaitingRoomStatus
)

enum class WaitingRoomStatus {
    Waiting,
    Playing
}