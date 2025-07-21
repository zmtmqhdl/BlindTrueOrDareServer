package org.example.model

import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class Message(
    val type: MessageType,
    val data: String? = null,
    val timestamp: Long
)

@Serializable
data class MessageDto(
    val type: MessageType,
    val data: String? = null,
    val timestamp: Long
)

enum class MessageType {
    UPDATE, SEND_START,
}