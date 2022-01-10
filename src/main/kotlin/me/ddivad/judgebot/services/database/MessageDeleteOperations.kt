package me.ddivad.judgebot.services.database

import dev.kord.core.entity.Member
import me.ddivad.judgebot.dataclasses.MessageDelete
import me.jakejmattson.discordkt.annotations.Service
import org.litote.kmongo.and
import org.litote.kmongo.eq

@Service
class MessageDeleteOperations(connection: ConnectionService) {
    private val messageDeleteCollection = connection.db.getCollection<MessageDelete>("MessageDelete")

    suspend fun createMessageDeleteRecord(guildId: String, target: Member, messageLink: String?) {
        val record = MessageDelete(target.id.toString(), guildId, messageLink)
        messageDeleteCollection.insertOne(record)
    }

    suspend fun getMessageDeletesForMember(guildId: String, userId: String): List<MessageDelete> {
        return messageDeleteCollection.find(
            and(
                MessageDelete::guildId eq guildId,
                MessageDelete::userId eq userId,
            )
        ).toList()
    }
}