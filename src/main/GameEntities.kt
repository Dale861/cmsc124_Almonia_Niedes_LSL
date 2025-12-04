package main

sealed class GameEntity {
    abstract val name: String?
    abstract val type: String
}

data class ChampionEntity(
    override val name: String?,
    val level: Int = 1,
    var health: Double = 100.0,
    var mana: Double = 100.0,
    val abilities: MutableList<String> = mutableListOf(),
    val eventHandlers: MutableMap<String, () -> Unit> = mutableMapOf()
): GameEntity() {
    override val type = "Champion"

    fun onCastHealth(threshold: Double, action: String): ChampionEntity {
        eventHandlers["onHealthBelow"] = {
            if (health < threshold) {
                println("  [$name] Health below $threshold! Executing: $action")
            }
        }
        return this
    }
    fun onCastMana(threshold: Double, action: String): ChampionEntity {
        eventHandlers["onManaBelow"] = {
            if (mana < threshold) {
                println("  [$name] Mana below $threshold! Executing: $action")
            }
        }
        return this
    }
    fun recall(): ChampionEntity {
        println("  [$name] is recalling to base...")
        health += 50.0
        mana += 50.0
        return this
    }
    fun cast(abilityName: String, manaCost: Double): ChampionEntity {
        if (mana >= manaCost) {
            println("  [$name] casts $abilityName! (Cost: $manaCost mana)")
            mana -= manaCost
        } else {
            println("  [$name] cannot cast $abilityName - not enough mana!")
        }
        return this
    }
    fun attack(target: String): ChampionEntity {
        println("  [$name] attacks $target!")
        return this
    }
    fun moveTo(location: String): ChampionEntity {
        println("  [$name] moves to $location")
        return this
    }
    fun useItem(itemName: String): ChampionEntity {
        println("  [$name] uses $itemName")
        return this
    }
    fun addAbility(abilityName: String): ChampionEntity {
        abilities.add(abilityName)
        println("  [$name] learned $abilityName!")
        return this
    }
    fun checkEvents() {
        eventHandlers.values.forEach { it() }
    }
    override fun toString(): String {
        return if (name != null) {
            "Champion($name, Level: $level, HP: $health, Mana: $mana)"
        } else {
            "Champion(uninitialized)"
        }
    }
}

data class AbilityEntity(
    override val name: String?,
    var cooldown: Double = 0.0,
    val manaCost: Double = 0.0,
    val damage: Double = 0.0
): GameEntity() {
    override val type = "Ability"
    fun setCooldown(cd: Double): AbilityEntity {
        cooldown = cd; return this
    }
    fun reduceCooldown(amount: Double): AbilityEntity {
        cooldown = maxOf(0.0, cooldown - amount); return this
    }
    override fun toString(): String {
        return if (name != null) {
            "Ability($name, CD: ${cooldown}s, Cost: $manaCost, Damage: $damage)"
        } else {
            "Ability(uninitialized)"
        }
    }
}
data class ItemEntity(
    override val name: String?,
    var cost: Int = 0,  // Change val to var if you want to modify it later
    val stats: MutableMap<String, Double> = mutableMapOf()
): GameEntity() {
    override val type = "Item"
    fun addStat(statName: String, value: Double): ItemEntity {
        stats[statName] = value; return this
    }

    override fun toString(): String {
        return if (name != null) {
            val statsStr =
                if (stats.isEmpty()) "" else ", Stats: ${stats.entries.joinToString(", ") { "${it.key}: ${it.value}" }}"
            "Item($name, Cost: ${cost}g$statsStr)"
        } else {
            "Item(uninitialized)"
        }
    }
}
data class BuffEntity(
    override val name: String?,
    val duration: Double = 0.0,
    val stackable: Boolean = false
): GameEntity() {
    override val type = "Buff"
    override fun toString(): String {
        return if (name != null) {
            "Buff($name, Duration: ${duration}s)"
        } else {
            "Buff(uninitialized)"
        }
    }
}

class BuiltinFunction(
    val name: String,
    val implementation: (List<Any?>) -> Any?
) {
    override fun toString() = "<builtin function $name>"
}