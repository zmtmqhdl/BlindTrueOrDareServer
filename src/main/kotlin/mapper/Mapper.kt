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
        participantList = participantList.map { it.toDto() }.toMutableSet(),
        roomStatus = roomStatus,
        writeTime = writeTime,
        questionNumber = questionNumber,
        questionList = questionList.map { it.toDto() }
    )

fun MessageDto.toDomain(): Message =
    Message(
        type = type,
        player = player?.toDomain(),
        data = data,
        timestamp = timestamp
    )

fun Message.toDto(): MessageDto =
    MessageDto(
        type = type,
        player = player?.toDto(),
        data = data,
        timestamp = timestamp
    )

fun QuestionDto.toDomain(): Question =
    Question(
        questionId = questionId,
        player = player.toDomain(),
        question = question,
        oVoter = oVoter.map { it.toDomain() }.toMutableSet(),
        xVoter = xVoter.map { it.toDomain() }.toMutableSet(),
        noAnswer = noAnswer.map { it.toDomain() }.toMutableSet()
    )

fun Question.toDto(): QuestionDto =
    QuestionDto(
        questionId = questionId,
        player = player.toDto(),
        question = question,
        oVoter = oVoter.map { it.toDto() }.toSet(),
        xVoter = xVoter.map { it.toDto() }.toSet(),
        noAnswer = noAnswer.map { it.toDto() }.toSet()
    )

fun AnswerDto.toDomain(): Answer =
    Answer(
        questionId = questionId,
        player = player.toDomain(),
        answer = answer
    )
