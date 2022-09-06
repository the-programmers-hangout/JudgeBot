package me.ddivad.judgebot.services.database.migrations

import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
    println("Running v1 DB Migration")
    db.createCollection("Guilds")
    db.createCollection("JoinLeaves")
    db.createCollection("Users")
}