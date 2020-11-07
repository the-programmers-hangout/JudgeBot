package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import com.gitlab.kordlib.gateway.RequestGuildMembers
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.dsl.listeners
import org.joda.time.DateTime

fun onGuildMemberLeave(loggingService: LoggingService, databaseService: DatabaseService) = listeners {
    on<GuildCreateEvent> {
        gateway.send(RequestGuildMembers(guildId = listOf(guild.id.value)))
    }

    on<MemberLeaveEvent> {
        val userRecord = databaseService.users.getOrCreateUser(this.user.asUser(), this.guild.asGuild())
        databaseService.users.addGuildLeave(userRecord, this.getGuild(), DateTime.now().millis)
    }

    on<MemberJoinEvent> {
        databaseService.users.getUserOrNull(this.member, this.guild.asGuild())?.let {
            databaseService.users.addGuildJoin(guild.asGuild(), it, this.member.joinedAt.toEpochMilli())
        }
    }
}
