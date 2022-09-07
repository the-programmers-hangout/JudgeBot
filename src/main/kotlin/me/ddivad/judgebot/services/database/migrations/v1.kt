package me.ddivad.judgebot.services.database.migrations

import me.ddivad.judgebot.services.database.GuildOperations
import me.ddivad.judgebot.services.database.JoinLeaveOperations
import me.ddivad.judgebot.services.database.MessageDeleteOperations
import me.ddivad.judgebot.services.database.UserOperations
import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
    val logger = KotlinLogging.logger {  }

    logger.info { "Running v1 DM Migration" }
    db.createCollection(GuildOperations.name)
    db.createCollection(JoinLeaveOperations.name)
    db.createCollection(UserOperations.name)
    db.createCollection(MessageDeleteOperations.name)
}