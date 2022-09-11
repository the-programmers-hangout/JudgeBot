package me.ddivad.judgebot.services.database

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import me.ddivad.judgebot.dataclasses.*
import me.jakejmattson.discordkt.annotations.Service
import org.litote.kmongo.eq

@Service
class GuildOperations(connection: ConnectionService) {
    companion object: Collection("Guilds")

    private val guildCollection = connection.db.getCollection<GuildInformation>(name)

    suspend fun setupGuild(guild: Guild): GuildInformation {
        val guildConfig = GuildInformation(guild.id.toString(), guild.name)
        this.guildCollection.insertOne(guildConfig)
        return guildConfig
    }

    suspend fun getRules(guild: Guild): List<Rule> {
        val guildInfo = this.getGuild(guild)
        return guildInfo.rules.filter { it.number != 0 }.sortedBy { it.number }
    }

    suspend fun getRulesForInfractionPrompt(guild: Guild): List<Rule> {
        val guildInfo = this.getGuild(guild)
        return guildInfo.rules.sortedBy { it.number }
    }

    suspend fun getRule(guild: Guild, ruleId: Int): Rule? {
        val guildInfo = this.getGuild(guild)
        return guildInfo.rules.find { it.number == ruleId }
    }

    suspend fun addRule(guild: Guild, rule: Rule) {
        val guildInfo = this.getGuild(guild)
        updateGuild(guildInfo.addRule(rule))
    }

    suspend fun editRule(guild: Guild, oldRule: Rule, updatedRule: Rule) {
        val guildInfo = this.getGuild(guild)
        guildInfo.editRule(oldRule, updatedRule)
        updateGuild(guildInfo)
    }

    suspend fun archiveRule(guild: Guild, ruleNumber: Int) {
        val guildInfo = this.getGuild(guild)
        guildInfo.archiveRule(ruleNumber)
        updateGuild(guildInfo)
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
        return this.getGuild(guild).getPunishmentByType(type, member.asUser().id.toString())
    }

    suspend fun getPunishmentByType(guild: Guild, userId: String, type: InfractionType): List<Punishment> {
        return this.getGuild(guild).getPunishmentByType(type, userId)
    }

    suspend fun getPunishmentsForUser(guild: Guild, user: User): List<Punishment> {
        return this.getGuild(guild).getPunishmentsByUser(user.id.toString())
    }

    suspend fun getBanOrNull(guild: Guild, userId: String): Ban? {
        return this.getGuild(guild).bans.find { it.userId == userId }
    }

    suspend fun getPunishmentsForGuild(guild: Guild, type: InfractionType): List<Punishment> {
        return this.getGuild(guild).punishments.filter { it.type == type }
    }

    suspend fun getActivePunishments(guild: Guild): List<Punishment> {
        return this.getGuild(guild).punishments
    }

    private suspend fun getGuild(guild: Guild): GuildInformation {
        return guildCollection.findOne(GuildInformation::guildId eq guild.id.toString())
            ?: GuildInformation(guild.id.toString(), guild.name)
    }

    private suspend fun updateGuild(guildInformation: GuildInformation): GuildInformation {
        guildCollection.updateOne(GuildInformation::guildId eq guildInformation.guildId, guildInformation)
        return guildInformation
    }
}