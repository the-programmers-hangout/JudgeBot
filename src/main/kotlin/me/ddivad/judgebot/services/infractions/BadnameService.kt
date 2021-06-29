package me.ddivad.judgebot.services.infractions

import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import me.jakejmattson.discordkt.api.annotations.Service

val names = mutableListOf<String>(
    "Stephen","Bob","Joe","Timmy","Arnold","Jeff","Tim","Doug"
)

@Service
class BadnameService() {
    suspend fun chooseRandomNickname(member: Member) {
        member.edit { nickname = names.random() }
    }
}