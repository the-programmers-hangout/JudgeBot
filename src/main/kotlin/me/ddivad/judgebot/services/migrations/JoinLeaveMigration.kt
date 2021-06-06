package me.ddivad.judgebot.services.migrations

import me.ddivad.judgebot.dataclasses.GuildLeave
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.JoinLeave
import me.ddivad.judgebot.services.database.ConnectionService
import me.jakejmattson.discordkt.api.annotations.Service
import org.litote.kmongo.`in`

@Service
class JoinLeaveMigration(connection: ConnectionService) {

    private val userCollection = connection.db.getCollection<GuildMember>("Users")
    private val joinLeaveCollection = connection.db.getCollection<JoinLeave>("JoinLeaves")

    suspend fun run() {
        println(
            """
                ---------------------------------
                Migration Starting :: Join Leaves
                ---------------------------------
            """.trimIndent()
        )

        val users = getAllUsers()
        val usersToDelete = getUsersToDelete(users)
        println("Found ${users.size} users, ${usersToDelete.size} empty records.")
        println("${users.size - usersToDelete.size} records will remain.")

        migrateJoinLeaveData(users)

        println("Deleting Empty Records...")
        userCollection.deleteMany(GuildMember::userId `in` usersToDelete)
        println("Empty records deleted.")
        println(
            """
                ---------------------------------
                Migration Complete :: Join Leaves
                ---------------------------------
            """.trimIndent()
        )
    }

    private suspend fun getAllUsers(): List<GuildMember> {
        return userCollection.find().toList()
    }

    private fun getUsersToDelete(allUsers: List<GuildMember>): MutableList<String> {
        val idsToDelete = mutableListOf<String>()
        allUsers.forEach { member ->
            val emptyGuilds = member.guilds.filter { guild ->
                guild.info.isEmpty() &&
                        guild.infractions.isEmpty() &&
                        guild.notes.isEmpty() &&
                        guild.linkedAccounts.isEmpty() &&
                        guild.deletedMessageCount.deleteReaction == 0
            }
            if (member.guilds.size == emptyGuilds.size) {
                idsToDelete.add(member.userId)
            }
        }
        return idsToDelete
    }

    private suspend fun migrateJoinLeaveData(users: List<GuildMember>) {
        println("Migrating Join Leave Data...")
        users.forEach { user ->
            user.guilds.forEach { guild ->
                addJoinLeaveData(guild.guildId, user.userId, guild.leaveHistory)
                guild.leaveHistory.clear()
            }
        }
        println("Join Leave Data migrated.")
    }

    private suspend fun addJoinLeaveData(guildId: String, memberId: String, joinLeaves: List<GuildLeave>) {
        joinLeaves.forEach {
            val newJoinLeave = JoinLeave(guildId, memberId, it.joinDate, it.leaveDate)
            joinLeaveCollection.insertOne(newJoinLeave)
        }
    }
}