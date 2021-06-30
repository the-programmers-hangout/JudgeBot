package me.ddivad.judgebot.util

import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun applyRoleWithTimer(member: Member, role: Role, millis: Long, fn: (Member) -> Unit): Job {
    member.addRole(role.id)
    return GlobalScope.launch {
        delay(millis)
        fn(member)
    }
}