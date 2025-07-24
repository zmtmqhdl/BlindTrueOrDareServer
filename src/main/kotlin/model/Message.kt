package org.example.model

import kotlinx.serialization.Serializable

data class Message(
    val type: MessageType,
    val senderId: String? = null,
    val data: String? = null,
    val timestamp: Long
)

@Serializable
data class MessageDto(
    val type: MessageType,
    val senderId: String? = null,
    val data: String? = null,
    val timestamp: Long
)

enum class MessageType {
    UPDATE, SEND_START, START
}