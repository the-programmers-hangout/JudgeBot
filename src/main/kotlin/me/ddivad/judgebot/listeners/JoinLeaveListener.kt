package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import com.gitlab.kordlib.gateway.RequestGuildMembers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.dsl.listeners

@Suppress("unused")
fun onGuildMemberLeave(databaseService: DatabaseService) = listeners {
    on<GuildCreateEvent> {
        gateway.send(RequestGuildMembers(guildId = listOf(guild.id.value)))
    }

    on<MemberLeaveEvent> {
        databaseService.joinLeaves.addLeaveData(guildId.value, user.id.value)
    }

    on<MemberJoinEvent> {
        // Add delay before creating user in case they are banned immediately (raid, etc...)
        GlobalScope.launch {
            delay(1000 * 60 * 5)
            guild.getMemberOrNull(member.id)?.let {
                databaseService.joinLeaves.createJoinLeaveRecord(guildId.value, member)
            }
        }
    }
}
