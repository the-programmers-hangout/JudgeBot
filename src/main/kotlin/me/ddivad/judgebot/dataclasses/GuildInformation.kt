package me.ddivad.judgebot.dataclasses

data class GuildInformation(
        val guildId: String,
        val rules: MutableList<Rule> = mutableListOf()
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
}

data class Rule(
        val number: Int,
        val title: String,
        val description: String,
        val link: String,
        var archived: Boolean = false
)
