package me.ddivad.judgebot.extensions

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.User

suspend fun User.testDmStatus() {
    getDmChannel().createMessage("Infraction message incoming").delete()
}

fun Message.jumpLink(guildId:String) = "https://discord.com/channels/${guildId}/${channel.id.value}/${id.value}"