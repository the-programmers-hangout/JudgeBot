package me.ddivad.judgebot.extensions

import dev.kord.core.entity.Message
import dev.kord.core.entity.User

suspend fun User.testDmStatus() {
    getDmChannel().createMessage("Infraction message incoming").delete()
}

fun Message.jumpLink(guildId:String) = "https://discord.com/channels/${guildId}/${channel.id.value}/${id.value}"