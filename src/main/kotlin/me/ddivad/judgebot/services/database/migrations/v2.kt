package me.ddivad.judgebot.services.database.migrations

import me.ddivad.judgebot.dataclasses.GuildInformation
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.services.database.GuildOperations
import me.ddivad.judgebot.services.database.UserOperations
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

suspend fun v2(db: CoroutineDatabase) {
    println("Running v2 DB Migration")
    val userCollection = db.getCollection<GuildMember>(UserOperations.name)
    val guildCollection = db.getCollection<GuildInformation>(GuildOperations.name)

    guildCollection.find().consumeEach { guild ->
        guild.bans.forEach {
            it.thinIce = false
            it.unbanTime = null
            it.dateTime = null
        }
        guildCollection.updateOne(GuildInformation::guildId eq guild.guildId, guild)
    }

    guildCollection.find().consumeEach {
        it.bans.forEach { ban ->
            val user = userCollection.findOne(GuildMember::userId eq ban.userId)
            if (user != null) {
                user.guilds.find { g -> g.guildId == it.guildId }?.bans?.add(ban)
                userCollection.updateOne(GuildMember::userId eq user.userId, user)
            }
        }
    }
}