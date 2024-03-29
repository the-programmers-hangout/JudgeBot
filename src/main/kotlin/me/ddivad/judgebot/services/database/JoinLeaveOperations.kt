package me.ddivad.judgebot.services.database

import dev.kord.core.entity.Member
import me.ddivad.judgebot.dataclasses.JoinLeave
import me.jakejmattson.discordkt.annotations.Service
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant

@Service
class JoinLeaveOperations(connection: ConnectionService) {
    companion object: Collection("JoinLeaves")

    private val joinLeaveCollection = connection.db.getCollection<JoinLeave>(name)

    suspend fun createJoinLeaveRecord(guildId: String, target: Member) {
        val joinLeave = JoinLeave(guildId, target.id.toString(), target.joinedAt.toEpochMilliseconds())
        joinLeaveCollection.insertOne(joinLeave)
    }

    suspend fun addLeaveData(guildId: String, userId: String) {
        joinLeaveCollection.findOneAndUpdate(
            and(
                JoinLeave::guildId eq guildId,
                JoinLeave::userId eq userId,
                JoinLeave::leaveDate eq null
            ),
            setValue(JoinLeave::leaveDate, Instant.now().toEpochMilli())
        )
    }

    suspend fun getMemberJoinLeaveDataForGuild(guildId: String, userId: String): List<JoinLeave> {
        return joinLeaveCollection.find(
            and(
                JoinLeave::guildId eq guildId,
                JoinLeave::userId eq userId,
            )
        ).toList()
    }

    suspend fun createJoinLeaveRecordIfNotRecorded(guildId: String, target: Member) {
        if (this.getMemberJoinLeaveDataForGuild(guildId, target.id.toString()).isNotEmpty()) {
            return
        }
        this.createJoinLeaveRecord(guildId, target)
    }
}