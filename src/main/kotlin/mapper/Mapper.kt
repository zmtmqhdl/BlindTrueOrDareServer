package org.example.mapper

import org.example.model.*

fun PlayerDto.toDomain(): Player =
    Player(
        playerId = playerId,
        nickname = nickname
    )

fun Player.toDto(): PlayerDto =
    PlayerDto(
        playerId = playerId,
        nickname = nickname
    )

fun Room.toDto(): RoomDto =
    RoomDto(
        roomId = roomId,
        hostId = hostId,
        participantList = participantList.map { it.toDto()},
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber
    )

fun MessageDto.toDomain(): Message =
    Message(
        type = type,
        senderId = senderId,
        data = data,
        timestamp = timestamp
    )

fun Message.toDto(): MessageDto =
    MessageDto(
        type = type,
        senderId = senderId,
        data = data,
        timestamp = timestamp
    )

fun RoomDto.toDomain(): Room =
    Room(
        roomId = roomId,
        hostId = hostId,
        participantList = participantList.map { it.toDomain() }.toMutableList(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber
    )