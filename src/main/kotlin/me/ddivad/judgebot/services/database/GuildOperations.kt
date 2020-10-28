package me.ddivad.judgebot.services.database

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.*
import me.jakejmattson.discordkt.api.annotations.Service
import org.litote.kmongo.eq

// Note: RunBlocking is needed for DB operations in this service, as they are used in a conversation (which does not support "suspend" functions)

@Service
class GuildOperations(private val connection: ConnectionService) {
    private val guildCollection = connection.db.getCollection<GuildInformation>("Guilds")

    suspend fun setupGuild(guild: Guild): GuildInformation {
        val guildConfig = GuildInformation(guild.id.value, guild.name)
        this.guildCollection.insertOne(guildConfig)
        return guildConfig
    }

    suspend fun getRules(guild: Guild): List<Rule>? {
        return runBlocking {
            guildCollection.findOne(GuildInformation::guildId eq guild.id.value)?.rules?.sortedBy { it.number }
        }
    }

    suspend fun getRule(guild: Guild, ruleId: Int): Rule? {
        return runBlocking {
            guildCollection.findOne(GuildInformation::guildId eq guild.id.value)?.rules?.find { it.number == ruleId }
        }
    }

    fun addRule(guild: Guild, rule: Rule) {
        runBlocking {
            val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
            guildInfo!!.addRule(rule)
            updateGuild(guildInfo)
        }
    }

    fun editRule(guild: Guild, oldRule: Rule, updatedRule: Rule) {
        runBlocking {
            val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
            guildInfo!!.editRule(oldRule, updatedRule)
            updateGuild(guildInfo)
        }
    }

    fun archiveRule(guild: Guild, ruleNumber: Int) {
        runBlocking {
            val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
            guildInfo!!.archiveRule(ruleNumber)
            updateGuild(guildInfo)
        }
    }

    suspend fun addPunishment(guild: Guild, punishment: Punishment) {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
        guildInfo!!.addPunishment(punishment)
        updateGuild(guildInfo)
    }

    suspend fun removePunishment(guild: Guild, userId: String, type: InfractionType) {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
        guildInfo!!.removePunishment(userId, type)
        updateGuild(guildInfo)
    }

    suspend fun banUser(guild: Guild, userId: String, moderator: String, reason: String): Ban {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
        val ban = Ban(userId, moderator, reason)
        guildInfo!!.addBan(ban)
        updateGuild(guildInfo)
        return ban
    }

    suspend fun removeBan(guild: Guild, userId: String) {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
        guildInfo!!.removeBan(userId)
    }

    suspend fun checkPunishmentExists(guild: Guild, member: Member, type: InfractionType): List<Punishment> {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.asGuild().id.value)
        return guildInfo!!.findPunishmentByType(type, member.asUser().id.value)
    }

    suspend fun getPunishmentsForGuild(guild: Guild): MutableList<Punishment> {
        val guildInfo = guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
        return guildInfo!!.punishments
    }

    private suspend fun updateGuild(guildInformation: GuildInformation): GuildInformation {
        guildCollection.updateOne(GuildInformation::guildId eq guildInformation.guildId, guildInformation)
        return guildInformation
    }
}