package me.ddivad.judgebot.services

import me.ddivad.judgebot.services.database.GuildOperations
import me.ddivad.judgebot.services.database.JoinLeaveOperations
import me.ddivad.judgebot.services.database.UserOperations
import me.jakejmattson.discordkt.api.annotations.Service

@Service
open class DatabaseService(
    val users: UserOperations,
    val guilds: GuildOperations,
    val joinLeaves: JoinLeaveOperations
) {
}