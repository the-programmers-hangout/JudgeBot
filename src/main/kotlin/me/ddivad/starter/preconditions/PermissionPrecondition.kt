package me.ddivad.starter.preconditions

import me.ddivad.starter.services.DEFAULT_REQUIRED_PERMISSION
import me.ddivad.starter.services.PermissionsService
import me.ddivad.starter.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.dsl.*

class PermissionPrecondtion(private val permissionsService: PermissionsService) : Precondition() {
    override suspend fun evaluate(event: CommandEvent<*>): PreconditionResult {
        val command = event.command
        val requiredPermissionLevel = command?.requiredPermissionLevel ?: DEFAULT_REQUIRED_PERMISSION
        val guild = event.guild!!

        if (!permissionsService.hasClearance(guild, event.author, requiredPermissionLevel))
            return Fail("You do not have the required permissions to perform this action.")

        return Pass
    }

}