package org.example.mapper

import org.example.model.CreateWaitingRoomRequest
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

fun WaitingRoom.toDto(): WaitingRoomDto =
    WaitingRoomDto(
        roomId = roomId,
        hostId = hostId,
        participantList = participantList,
        status = status
    )