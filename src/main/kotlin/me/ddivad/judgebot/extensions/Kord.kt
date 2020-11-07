package me.ddivad.judgebot.extensions

import com.gitlab.kordlib.core.entity.User

suspend fun User.testDmStatus() {
    getDmChannel().createMessage("Infraction message incoming").delete()
}