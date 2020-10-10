package me.ddivad.judgebot.services.database

import me.ddivad.judgebot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.annotations.Service
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

@Service
class ConnectionService(val config: Configuration) {
    private val client: CoroutineClient = KMongo.createClient(config.dbConfiguration.address).coroutine
    val db: CoroutineDatabase = client.getDatabase(config.dbConfiguration.databaseName)
}