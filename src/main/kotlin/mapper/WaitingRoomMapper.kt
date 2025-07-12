package org.example.mapper

import org.example.model.CreateWaitingRoomRequest
import org.example.model.Player
import org.example.model.PlayerDto
import org.example.model.WaitingRoom
import org.example.model.WaitingRoomDto
import org.example.model.WaitingRoomStatus

//fun CreateWaitingRoomRequest.toWaitingRoom(): WaitingRoom =
//    WaitingRoom(
//        roomId =
//        hostId = hostId,
//        participantList = listOf(user),
//        status = WaitingRoomStatus.Waiting
//    )

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

fun WaitingRoom.toDto(): WaitingRoomDto =
    WaitingRoomDto(
        roomId = roomId,
        hostId = hostId,
        participantList = participantList.map { it.toDto()},
        status = status
    )