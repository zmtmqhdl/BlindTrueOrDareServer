package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: String,
    val nickname: String
)

@Serializable
data class CreateWaitingRoomRequest(
    val user: User
)

@Serializable
data class CreateWaitingRoomResponse(
    val waitingRoomId: String
)


@Serializable
data class WaitingRoom(
    val roomId: String,
    val hostId: String,
    val participantList: List<User>,
    val status: WaitingRoomStatus
)

@Serializable
data class WaitingRoomDto(
    val roomId: String,
    val hostId: String,
    val participantList: List<User>,
    val status: WaitingRoomStatus
)

enum class WaitingRoomStatus {
    Waiting,
    Playing
}