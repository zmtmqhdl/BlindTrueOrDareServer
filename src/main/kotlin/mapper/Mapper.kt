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
        host = host.toDto(),
        participantList = participantList.map { it.toDto()}.toMutableSet(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber,
        questionList = questionList.map { it.toDto() }
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
        host = host.toDomain(),
        participantList = participantList.map { it.toDomain() }.toMutableSet(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber,
        questionList = questionList.map { it.toDomain()}.toMutableList()
    )

fun QuestionDto.toDomain(): Question =
    Question(
        questionId = questionId,
        playerId = playerId,
        question = question,
        oVoters = oVoters.toMutableSet(),
        xVoters = xVoters.toMutableSet()
    )

fun Question.toDto(): QuestionDto =
    QuestionDto(
        questionId = questionId,
        playerId = playerId,
        question = question,
        oVoters = oVoters,
        xVoters = xVoters
    )

fun Answer.toDto(): AnswerDto =
    AnswerDto(
        questionId = questionId,
        playerId = playerId,
        answer = answer
    )

fun AnswerDto.toDomain(): Answer =
    Answer(
        questionId = questionId,
        playerId = playerId,
        answer = answer
    )
