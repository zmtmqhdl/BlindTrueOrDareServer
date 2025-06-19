package org.example.databaseFactory

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.database.ParticipantLists
import org.example.database.Player
import org.example.database.Question
import org.example.database.WaitingRoom
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun connectToDatabase() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/BlindTrueOrDare"
        driverClassName = "org.postgresql.Driver"
        username = "postgres"
        password = "xogns1469"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Player,
            WaitingRoom,
            ParticipantLists,
            Question
        )
    }
}