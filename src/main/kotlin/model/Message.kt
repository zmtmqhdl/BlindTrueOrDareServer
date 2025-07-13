package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val type: MessageType,
    val data: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    Enter, Update,
}