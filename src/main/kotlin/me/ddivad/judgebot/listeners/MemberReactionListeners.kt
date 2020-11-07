package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.extensions.jumpLink
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake

fun onMemberReactionAdd(configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.longValue]
        if (!guildConfiguration?.reactions!!.enabled) return@on

        if(this.emoji.name == guildConfiguration.reactions.flagMessageReaction) {
            guild.asGuild().getChannelOf<TextChannel>(guildConfiguration.loggingConfiguration.alertChannel.toSnowflake())
                    .asChannel().createMessage("User ${user.mention} flagged the message: " +
                            "${this.message.asMessage().jumpLink(guild.id.value)} in: ${this.channel.mention}")
        }
    }
}
