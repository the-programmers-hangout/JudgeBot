package me.ddivad.judgebot.services.database

import me.ddivad.judgebot.dataclasses.Meta
import me.jakejmattson.discordkt.annotations.Service

@Service
class MetaOperations(connection: ConnectionService) {
    companion object: Collection("Meta")
    private val metaCollection = connection.db.getCollection<Meta>(name)

    suspend fun getCurrentVersion() = metaCollection.findOne()
    suspend fun save(meta: Meta) = metaCollection.save(meta)
}