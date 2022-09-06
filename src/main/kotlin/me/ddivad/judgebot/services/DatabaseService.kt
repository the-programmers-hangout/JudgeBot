package me.ddivad.judgebot.services

import me.ddivad.judgebot.services.database.*
import me.jakejmattson.discordkt.annotations.Service

@Service
open class DatabaseService(
    val users: UserOperations,
    val guilds: GuildOperations,
    val joinLeaves: JoinLeaveOperations,
    val meta: MetaOperations,
    val messageDeletes: MessageDeleteOperations
)