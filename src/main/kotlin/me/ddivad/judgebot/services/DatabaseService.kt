package me.ddivad.judgebot.services

import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.database.UserOperations
import me.jakejmattson.discordkt.api.annotations.Service

@Service
open class DatabaseService(val config: Configuration,
                           val users: UserOperations) {
}