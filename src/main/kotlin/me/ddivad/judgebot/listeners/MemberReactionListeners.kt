package me.ddivad.judgebot.listeners

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.util.createFlagMessage
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake

@Suppress("unused")
fun onMemberReactionAdd(configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.value]
        if (!guildConfiguration?.reactions!!.enabled) return@on

        when (this.emoji.name) {
            guildConfiguration.reactions.flagMessageReaction -> {
                message.deleteReaction(this.emoji)
                val channel = message.getChannel()
                guild.asGuild()
                    .getChannelOf<TextChannel>(guildConfiguration.loggingConfiguration.alertChannel.toSnowflake())
                    .asChannel()
                    .createMessage(createFlagMessage(user.asUser(), message.asMessage(), channel))
                    .addReaction(Emojis.question)
            }
        }
    }
}
