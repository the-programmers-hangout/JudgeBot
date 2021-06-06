package me.ddivad.judgebot.dataclasses

data class GuildInformation(
    val guildId: String,
    val guildName: String,
    val rules: MutableList<Rule> = mutableListOf(),
    val bans: MutableList<Ban> = mutableListOf(),
    val punishments: MutableList<Punishment> = mutableListOf()
) {
    fun addRule(rule: Rule): GuildInformation = this.apply {
        this.rules.add(rule)
    }

    fun archiveRule(ruleNumber: Int): GuildInformation = this.apply {
        this.rules.find { it.number == ruleNumber }?.archived = true
    }

    fun editRule(oldRule: Rule, updatedRule: Rule): Rule {
        val index = this.rules.indexOf(oldRule)
        this.rules[index] = updatedRule
        return updatedRule
    }

    fun addPunishment(punishment: Punishment): GuildInformation = this.apply {
        val nextId: Int = if (this.punishments.isEmpty()) 1 else this.punishments.maxByOrNull { it.id }!!.id + 1
        punishment.id = nextId
        this.punishments.add(punishment)
    }

    fun removePunishment(userId: String, type: InfractionType): GuildInformation = this.apply {
        val punishment = this.getPunishmentByType(type, userId).first()
        this.punishments.remove(punishment)
    }

    fun getPunishmentByType(type: InfractionType, userId: String): List<Punishment> {
        return this.punishments.filter { it.type == type && it.userId == userId }
    }

    fun getPunishmentsByUser(userId: String): List<Punishment> {
        return this.punishments.filter { it.userId == userId }
    }

    fun checkBanExits(userId: String): Boolean {
        return this.bans.any { it.userId == userId }
    }

    fun addBan(ban: Ban): GuildInformation = this.apply {
        this.bans.add(ban)
    }

    fun removeBan(userId: String): GuildInformation = this.apply {
        val ban = this.bans.find { it.userId == userId }
        this.bans.remove(ban)
    }
}

data class Rule(
    val number: Int,
    val title: String,
    val description: String,
    val link: String,
    var archived: Boolean = false
)
