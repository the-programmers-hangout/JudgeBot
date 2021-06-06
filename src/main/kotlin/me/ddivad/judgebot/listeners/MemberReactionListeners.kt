package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.extensions.jumpLink
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake

@Suppress("unused")
fun onMemberReactionAdd(configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.longValue]
        if (!guildConfiguration?.reactions!!.enabled) return@on

        when (this.emoji.name) {
            guildConfiguration.reactions.flagMessageReaction -> {
                message.deleteReaction(this.emoji)
                guild.asGuild()
                    .getChannelOf<TextChannel>(guildConfiguration.loggingConfiguration.alertChannel.toSnowflake())
                    .asChannel()
                    .createMessage(
                        "**Message Flagged**" +
                                "\n**User**: ${user.mention}" +
                                "\n**Channel**: ${message.channel.mention}" +
                                "\n**Author:** ${message.asMessage().author?.mention}" +
                                "\n**Message:** ${message.asMessage().jumpLink(guild.id.value)}"
                    )
                    .addReaction(Emojis.question)
            }
        }
    }
}
