package me.ddivad.judgebot.extensions

import dev.kord.core.entity.User

suspend fun User.testDmStatus() {
    getDmChannel().createMessage("Infraction message incoming").delete()
}