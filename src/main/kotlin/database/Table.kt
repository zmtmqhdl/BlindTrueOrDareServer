package org.example.database

import org.example.model.WaitingRoomStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Player : Table("player") {
    val playerId = uuid("player_id")
    val nickname = text("nickname")

    override val primaryKey = PrimaryKey(playerId)
}

object WaitingRoom : Table("waiting_room") {
    val roomId = uuid("room_id")
    val waitingRoomStatus = enumerationByName("waiting_room_status", 10, WaitingRoomStatus::class)
}

object ParticipantLists : Table("participant_list") {
    val roomId = uuid("room_id").references(WaitingRoom.roomId, onDelete = ReferenceOption.CASCADE)
    val playerId = uuid("player_id").references(Player.playerId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(roomId, playerId)
}

object Question : Table("question") {
    val questionId = uuid("question_id")
    val roomId = uuid("room_id").references(WaitingRoom.roomId, onDelete = ReferenceOption.CASCADE)
    val playerId = uuid("player_id").references(Player.playerId, onDelete = ReferenceOption.SET_NULL).nullable()
    val text = text("text")
    val oCount = integer("o_count").default(0)
    val xCount = integer("x_count").default(0)

    override val primaryKey = PrimaryKey(questionId)
}
