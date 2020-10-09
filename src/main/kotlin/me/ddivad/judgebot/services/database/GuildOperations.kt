package me.ddivad.judgebot.services.database

import com.gitlab.kordlib.core.entity.Guild
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.GuildInformation
import me.ddivad.judgebot.dataclasses.Rule
import me.jakejmattson.discordkt.api.annotations.Service
import org.litote.kmongo.eq

// Note: RunBlocking is needed for DB operations in this service, as they are used in a conversation (which does not support "suspend" functions)

@Service
class GuildOperations(private val connection: ConnectionService) {
    private val guildCollection = connection.db.getCollection<GuildInformation>("Guilds")

    suspend fun setupGuild(guild: Guild): GuildInformation {
        val guildConfig = GuildInformation(guild.id.value)
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

    private suspend fun updateGuild(guildInformation: GuildInformation): GuildInformation {
        guildCollection.updateOne(GuildInformation::guildId eq guildInformation.guildId, guildInformation)
        return guildInformation
    }
}