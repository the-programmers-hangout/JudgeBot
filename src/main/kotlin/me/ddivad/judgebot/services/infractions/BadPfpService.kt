package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.behavior.ban
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.embeds.createBadPfpEmbed
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Service
class BadPfpService(private val muteService: MuteService,
                    private val discord: Discord,
                    private val loggingService: LoggingService) {
    private val badPfpTracker = hashMapOf<Pair<GuildID, UserId>, Job>()
    private suspend fun toKey(member: Member): Pair<GuildID, UserId> = member.guild.id.value to member.asUser().id.value

    suspend fun applyBadPfp(target: Member, guild: Guild, badPfp: Infraction, timeLimit: Long) {
        target.sendPrivateMessage {
            createBadPfpEmbed(guild, target)
        }
        muteService.applyMute(target, timeLimit, "Bad Pfp Mute")
        loggingService.badBfpApplied(guild, target)
        badPfpTracker[toKey((target))] = GlobalScope.launch {
            delay(timeLimit)
            if (target.avatar == discord.api.getUser(target.id)?.avatar) {
                GlobalScope.launch {
                    delay(1000)
                    guild.ban(target.id) {
                        reason = "BadPfp - Having a bad pfp and refusing to change it."
                        deleteMessagesDays = 1
                    }
                    loggingService.badPfpBan(guild, target)
                }
            } else {
                target.asUser().sendPrivateMessage("Thanks for changing you avatar. You will not be banned.")
            }
        }
    }

    suspend fun hasActiveBapPfp(target: Member): Boolean {
        return badPfpTracker.containsKey(toKey(target))
    }

    suspend fun cancelBadPfp(guild: Guild, target: Member) {
        val key = toKey(target)
        if (hasActiveBapPfp(target)) {
            badPfpTracker[key]?.cancel()
            badPfpTracker.remove(key)
            loggingService.badPfpCancelled(guild, target)
            muteService.removeMute(target)
        }
    }
}