package me.ddivad.judgebot.services.database

import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import me.ddivad.judgebot.dataclasses.JoinLeave
import me.jakejmattson.discordkt.api.annotations.Service
import org.joda.time.DateTime
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

@Service
class JoinLeaveOperations(connection: ConnectionService) {
    private val joinLeaveCollection = connection.db.getCollection<JoinLeave>("JoinLeaves")

    suspend fun createJoinLeaveRecord(guildId: String, target: Member) {
        val joinLeave = JoinLeave(guildId, target.id.value, target.joinedAt.toEpochMilli())
        joinLeaveCollection.insertOne(joinLeave)
    }

    suspend fun addLeaveData(guildId: String, userId: String) {
        joinLeaveCollection.findOneAndUpdate(
            and(
                JoinLeave::guildId eq guildId,
                JoinLeave::userId eq userId,
                JoinLeave::leaveDate eq null
            ),
            setValue(JoinLeave::leaveDate, DateTime.now().millis)
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
        if (this.getMemberJoinLeaveDataForGuild(guildId, target.id.value).isNotEmpty()) {
            return
        }
        this.createJoinLeaveRecord(guildId, target)
    }
}