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
        participantList = participantList.map { it.toDto()}.toMutableSet(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber
    )

fun MessageDto.toDomain(): Message =
    Message(
        type = type,
        playerId = playerId,
        data = data,
        timestamp = timestamp
    )

fun Message.toDto(): MessageDto =
    MessageDto(
        type = type,
        playerId = playerId,
        data = data,
        timestamp = timestamp
    )

fun RoomDto.toDomain(): Room =
    Room(
        roomId = roomId,
        hostId = hostId,
        participantList = participantList.map { it.toDomain() }.toMutableSet(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber
    )

fun QuestionDto.toDomain(): Question =
    Question(
        playerId = playerId,
        question = question,
        oVoters = oVoters,
        xVoters = xVoters
    )

fun Question.toDto(): QuestionDto =
    QuestionDto(
        playerId = playerId,
        question = question,
        oVoters = oVoters,
        xVoters = xVoters
    )