package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import com.gitlab.kordlib.gateway.RequestGuildMembers
import kotlinx.coroutines.flow.toList
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.dsl.listeners
import org.joda.time.DateTime

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
        databaseService.users.getUserOrNull(this.member.asUser(), this.guild.asGuild())?.let {
            databaseService.users.addGuildJoin(this.getGuild(), it, this.member.joinedAt.toEpochMilli())
            return@on
        }
        databaseService.users.getOrCreateUser(this.member.asUser(), this.getGuild())
    }
}
