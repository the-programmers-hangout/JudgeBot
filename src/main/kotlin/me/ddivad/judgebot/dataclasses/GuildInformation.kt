package me.ddivad.judgebot.dataclasses

data class GuildInformation(
        val guildId: String,
        val guildName: String,
        val rules: MutableList<Rule> = mutableListOf(),
        val bans: MutableList<Ban> = mutableListOf(),
        val punishments: MutableList<Punishment> = mutableListOf()
) {
    fun addRule(rule: Rule) {
        this.rules.add(rule)
    }

    fun getRuleById(ruleNumber: Int): Rule? {
        return this.rules.firstOrNull { it.number == ruleNumber }
    }

    fun archiveRule(ruleNumber: Int) {
        this.rules.find { it.number == ruleNumber }!!.archived = true
    }

    fun editRule(oldRule: Rule, updatedRule: Rule): Rule {
        val index = this.rules.indexOf(oldRule)
        this.rules[index] = updatedRule
        return updatedRule
    }

    fun addPunishment(punishment: Punishment) {
        punishment.id = this.punishments.size + 1
        this.punishments.add(punishment)
    }

    fun removePunishment(userId: String, type: InfractionType) {
        val punishment = this.findPunishmentByType(type, userId).first()
        this.punishments.remove(punishment)
    }

    fun findPunishmentByType(type: InfractionType, userId: String): List<Punishment> {
        return this.punishments.filter { it.type == type && it.userId == userId }
    }

    fun addBan(ban: Ban) {
        this.bans.add(ban)
    }

    fun removeBan(userId: String) {
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
