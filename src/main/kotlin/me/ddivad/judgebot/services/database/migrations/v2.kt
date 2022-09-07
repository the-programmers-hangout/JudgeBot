package me.ddivad.judgebot.services.database.migrations

import com.mongodb.client.model.ReplaceOneModel
import me.ddivad.judgebot.dataclasses.Ban
import me.ddivad.judgebot.dataclasses.GuildInformation
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.services.database.GuildOperations
import me.ddivad.judgebot.services.database.UserOperations
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.replaceOne

data class Result(val guildId: String, val bans: List<Ban>)

suspend fun v2(db: CoroutineDatabase) {
    println("Running v2 DB Migration")
    val userCollection = db.getCollection<GuildMember>(UserOperations.name)
    val guildCollection = db.getCollection<GuildInformation>(GuildOperations.name)
    val guildBans = guildCollection.find().toList().map { Result(it.guildId, it.bans) }
    val banDocuments = mutableListOf<ReplaceOneModel<GuildInformation>>()
    guildCollection.find().consumeEach { guild ->
        guild.bans.forEach {
            it.thinIce = false
            it.unbanTime = null
            it.dateTime = null
        }
        banDocuments.add(replaceOne(GuildInformation::guildId eq guild.guildId, guild))
    }
    if (banDocuments.isNotEmpty()) {
        guildCollection.bulkWrite(requests = banDocuments)
    }

    val userDocuments = mutableListOf<ReplaceOneModel<GuildMember>>()
    userCollection.find().consumeEach { user ->
        guildBans.forEach { gb ->
            val userBan = gb.bans.find { it.userId == user.userId }
            if (userBan != null) {
                user.guilds.find { it.guildId == gb.guildId }?.bans?.add(userBan)
            }
        }
        user.guilds.forEach { guildDetails -> guildDetails.pointDecayFrozen = false }
        userDocuments.add(replaceOne(GuildMember::userId eq user.userId, user))
    }
    if (userDocuments.isNotEmpty()) {
        userCollection.bulkWrite(requests = userDocuments)
    }
}