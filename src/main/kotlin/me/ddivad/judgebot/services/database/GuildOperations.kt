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
class GuildOperations(connection: ConnectionService) {
    private val guildCollection = connection.db.getCollection<GuildInformation>("Guilds")

    suspend fun setupGuild(guild: Guild): GuildInformation {
        val guildConfig = GuildInformation(guild.id.value, guild.name)
        this.guildCollection.insertOne(guildConfig)
        return guildConfig
    }

    suspend fun getRules(guild: Guild): List<Rule> {
        val guildInfo = this.getGuild(guild)
        return runBlocking {
            guildInfo.rules.sortedBy { it.number }
        }
    }

    suspend fun getRule(guild: Guild, ruleId: Int): Rule? {
        val guildInfo = this.getGuild(guild)
        return runBlocking {
            guildInfo.rules.find { it.number == ruleId }
        }
    }

    suspend fun addRule(guild: Guild, rule: Rule) {
        val guildInfo = this.getGuild(guild)
        updateGuild(guildInfo.addRule(rule))
    }

    suspend fun editRule(guild: Guild, oldRule: Rule, updatedRule: Rule) {
        val guildInfo = this.getGuild(guild)
        runBlocking {
            guildInfo.editRule(oldRule, updatedRule)
            updateGuild(guildInfo)
        }
    }

    suspend fun archiveRule(guild: Guild, ruleNumber: Int) {
        val guildInfo = this.getGuild(guild)
        runBlocking {
            guildInfo.archiveRule(ruleNumber)
            updateGuild(guildInfo)
        }
    }

    suspend fun addPunishment(guild: Guild, punishment: Punishment) {
        this.getGuild(guild).addPunishment(punishment).let { updateGuild(it) }
    }

    suspend fun removePunishment(guild: Guild, userId: String, type: InfractionType) {
        this.getGuild(guild).removePunishment(userId, type).let { updateGuild(it) }
    }

    suspend fun addBan(guild: Guild, ban: Ban): Ban {
        this.getGuild(guild).addBan(ban).let { updateGuild(it) }
        return ban
    }

    suspend fun editBanReason(guild: Guild, userId: String, reason: String) {
        val guildInfo = this.getGuild(guild)
        guildInfo.bans.find { it.userId == userId }?.reason = reason
        updateGuild(guildInfo)
    }

    suspend fun removeBan(guild: Guild, userId: String) {
        this.getGuild(guild).removeBan(userId).let { updateGuild(it) }
    }

    suspend fun checkBanExists(guild: Guild, userId: String): Boolean {
        return this.getGuild(guild).checkBanExits(userId)
    }

    suspend fun checkPunishmentExists(guild: Guild, member: Member, type: InfractionType): List<Punishment> {
        return this.getGuild(guild).getPunishmentByType(type, member.asUser().id.value)
    }

    suspend fun getPunishmentByType(guild: Guild, userId: String, type: InfractionType): List<Punishment> {
        return this.getGuild(guild).getPunishmentByType(type, userId)
    }

    suspend fun getPunishmentsForUser(guild: Guild, user: User): List<Punishment> {
        return this.getGuild(guild).getPunishmentsByUser(user.id.value)
    }

    suspend fun getBanOrNull(guild: Guild, userId: String): Ban? {
        return this.getGuild(guild).bans.find {it.userId == userId}
    }

    suspend fun getPunishmentsForGuild(guild: Guild, type: InfractionType): List<Punishment> {
        return this.getGuild(guild).punishments.filter { it.type == type }
    }

    suspend fun getActivePunishments(guild: Guild): List<Punishment> {
        return this.getGuild(guild).punishments
    }

    private suspend fun getGuild(guild: Guild): GuildInformation {
        return guildCollection.findOne(GuildInformation::guildId eq guild.id.value)
                ?: GuildInformation(guild.id.value, guild.name)
    }

    private suspend fun updateGuild(guildInformation: GuildInformation): GuildInformation {
        guildCollection.updateOne(GuildInformation::guildId eq guildInformation.guildId, guildInformation)
        return guildInformation
    }
}