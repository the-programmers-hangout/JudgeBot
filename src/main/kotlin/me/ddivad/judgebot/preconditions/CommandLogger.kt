package me.ddivad.judgebot.preconditions

import me.jakejmattson.discordkt.api.dsl.precondition
import me.jakejmattson.discordkt.api.extensions.sanitiseMentions

fun commandLogger() = precondition {
    command ?: return@precondition fail()

    val args = rawInputs.commandArgs.joinToString()

    if (args.length > 1500)
        return@precondition fail("Command is too long (${args.length} chars, max: 1500")

    if (guild != null) {
        val message =
            "${author.tag} :: ${author.id.value} :: " +
                    "Invoked `${command!!.names.first()}`" +
                    if (args.isEmpty()) "" else " Args: ${args.sanitiseMentions(discord)}"

        println(message)
        return@precondition
    }
}
