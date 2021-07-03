package me.ddivad.judgebot.listeners

import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.RequestGuildMembers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.dsl.listeners

@OptIn(PrivilegedIntent::class)
@Suppress("unused")
fun onGuildMemberLeave(databaseService: DatabaseService) = listeners {
    on<GuildCreateEvent> {
        gateway.send(RequestGuildMembers(guildId = guild.id))
    }

    on<MemberLeaveEvent> {
        databaseService.joinLeaves.addLeaveData(guildId.asString, user.id.asString)
    }

    on<MemberJoinEvent> {
        // Add delay before creating user in case they are banned immediately (raid, etc...)
        GlobalScope.launch {
            delay(1000 * 60 * 1)
            guild.getMemberOrNull(member.id)?.let {
                databaseService.joinLeaves.createJoinLeaveRecord(guildId.asString, member)
            }
        }
    }
}
