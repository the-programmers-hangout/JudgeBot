package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import com.gitlab.kordlib.gateway.RequestGuildMembers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.dsl.listeners
import org.joda.time.DateTime

@Suppress("unused")
fun onGuildMemberLeave(loggingService: LoggingService, databaseService: DatabaseService) = listeners {
    on<GuildCreateEvent> {
        gateway.send(RequestGuildMembers(guildId = listOf(guild.id.value)))
    }

    on<MemberLeaveEvent> {
        databaseService.users.getUserOrNull(this.user.asUser(), this.guild.asGuild())?.let {
            databaseService.users.addGuildLeave(it, guild.asGuild(), DateTime.now().millis)
        }
    }

    on<MemberJoinEvent> {
        val user = this.member.asUser()
        val guild = this.getGuild()
        databaseService.users.getUserOrNull(user, guild)?.let {
            databaseService.users.addGuildJoin(guild, it, this.member.joinedAt.toEpochMilli())
            return@on
        }
        // Add delay before creating user in case they are banned (raid, etc...)
        GlobalScope.launch {
            delay(1000 * 60 * 5)
            databaseService.users.getOrCreateUser(member, guild)
        }
    }
}
