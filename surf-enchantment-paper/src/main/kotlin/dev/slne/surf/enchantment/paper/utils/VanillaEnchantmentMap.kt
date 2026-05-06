package dev.slne.surf.enchantment.paper.utils

import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.object2ObjectMapOf
import dev.slne.surf.api.paper.builder.LoreBuilder
import dev.slne.surf.enchantment.api.utils.Enchantable
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.EnchantmentKeys
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.Range
import kotlin.math.ceil
import kotlin.math.min

enum class VanillaEnchantmentMap(
    override val key: Key,
    override val displayName: Component,
    override val description: LoreBuilder.(Int) -> Unit,
    override val rarity: Rarity,
    override val maxLevel: @Range(from = 1, to = 255) Int? = run {
        val enchantment =
            RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)

        enchantment.maxLevel
    }
) : Enchantable {
    UNBREAKING(
        EnchantmentKeys.UNBREAKING,
        displayName = text("Unbreaking"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht die Haltbarkeit von Gegenständen um den Faktor")
                    variableValue(" %placeholder%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (1 + level).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    SMITE(
        EnchantmentKeys.SMITE,
        displayName = text("Smite"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Gewährt einen")
                    variableValue(" %placeholder% ")
                    darkSpacer("Bonus auf Nahkampfschaden gegen untote Mobs")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (2.5 * level).toString(),
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    BANE_OF_ARTHROPODS(
        EnchantmentKeys.BANE_OF_ARTHROPODS,
        displayName = text("Bane of Arthropods"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verleiht einen")
                    variableValue(" %damage% ")
                    darkSpacer("Bonus auf Nahkampfschaden gegen Gliederfüßer")
                }
                line {
                    darkSpacer("und bis zu ")
                    variableValue(" %seconds% ")
                    darkSpacer("Sekunden Verlangsamung IV")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "damage" to (2.5 * level).toString(),
                    "seconds" to (0.5 * level).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    EFFICIENCY(
        EnchantmentKeys.EFFICIENCY,
        displayName = text("Efficiency"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht die Abbaugeschwindigkeit um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (20 + 5 * level).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    IMPALING(
        EnchantmentKeys.IMPALING,
        displayName = text("Impaling"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verursacht")
                    variableValue(" %placeholder% ")
                    darkSpacer("zusätzlichen Schaden an Meeresmobs")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 2.5).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    THORNS(
        EnchantmentKeys.THORNS,
        displayName = text("Thorns"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verleiht eine")
                    variableValue(" %placeholder%% ")
                    darkSpacer("Chance, einen Teil des")
                }
                line {
                    darkSpacer("eingehenden Schadens auf den Angreifer zu reflektieren")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 15).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    FEATHER_FALLING(
        EnchantmentKeys.FEATHER_FALLING,
        displayName = text("Feather Falling"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert Fallschaden um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 12).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    RESPIRATION(
        EnchantmentKeys.RESPIRATION,
        displayName = text("Respiration"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verlängert die Atemzeit unter Wasser um")
                    variableValue(" %seconds% ")
                    darkSpacer("Sekunden")
                }
                line {
                    darkSpacer("und gibt eine")
                    variableValue(" %chance%% ")
                    darkSpacer("Chance, Ertrinkungsschaden zu ignorieren")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "seconds" to (15 * level).toString(),
                    "chance" to (level.toDouble() / (level + 1) * 100).toInt().toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    PROJECTILE_PROTECTION(
        EnchantmentKeys.PROJECTILE_PROTECTION,
        displayName = text("Projectile Protection"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert eingehenden Projektilschaden um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 8).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    KNOCKBACK(
        EnchantmentKeys.KNOCKBACK,
        displayName = text("Knockback"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Gewährt einen")
                    variableValue(" %placeholder%% ")
                    darkSpacer("Bonus auf jeglichen Rückstoß")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 85 + 20).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    FIRE_ASPECT(
        EnchantmentKeys.FIRE_ASPECT,
        displayName = text("Fire Aspect"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Setzt Gegner für")
                    variableValue(" 4 ")
                    darkSpacer("Sekunden in Brand und")
                }
                line {
                    darkSpacer("verursacht bei jedem Feuertick")
                    variableValue(" %placeholder% ")
                    darkSpacer("Schaden")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to ((level * 4) - 1).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    LUCK_OF_THE_SEA(
        EnchantmentKeys.LUCK_OF_THE_SEA,
        displayName = text("Luck of the Sea"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht die Chance auf Schätze um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (2 * level).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    CHANNELING(
        EnchantmentKeys.CHANNELING,
        displayName = text("Channeling"),
        description = { _ ->
            line {
                darkSpacer("Bei Gewitter wird ein Blitz die Landeposition des Dreizack treffen")
            }
        },
        rarity = Rarity.EPIC,
    ),
    SHARPNESS(
        EnchantmentKeys.SHARPNESS,
        displayName = text("Sharpness"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Gewährt")
                    variableValue(" %placeholder% ")
                    darkSpacer("zusätzlichen Nahkampfschaden")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (0.5 * level + 1).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    SOUL_SPEED(
        EnchantmentKeys.SOUL_SPEED,
        displayName = text("Soul Speed"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht die Gehgeschwindigkeit auf Seelensand und Seelenboden um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to ((level * 0.105) + 1.3).toString()
                )
            }
        ),
        rarity = Rarity.EPIC,
    ),
    LOYALTY(
        EnchantmentKeys.LOYALTY,
        displayName = text("Loyalty"),
        description = { _ ->
            line {
                darkSpacer("Der Dreizack kehrt zurück, nachdem er geworfen wurde")
            }
        },
        rarity = Rarity.UNCOMMON,
    ),
    SILK_TOUCH(
        EnchantmentKeys.SILK_TOUCH,
        displayName = text("Silk Touch"),
        description = { _ ->
            line {
                darkSpacer("Erlaubt es viele Blöcke in ihrer Urform abzubauen")
            }
        },
        rarity = Rarity.EPIC,
    ),
    QUICK_CHARGE(
        EnchantmentKeys.QUICK_CHARGE,
        displayName = text("Quick Charge"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert die Ladezeit der Armbrust um")
                    variableValue(" %placeholder% ")
                    darkSpacer("Sekunden")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 0.25).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    AQUA_AFFINITY(
        EnchantmentKeys.AQUA_AFFINITY,
        displayName = text("Aqua Affinity"),
        description = { _ ->
            line {
                darkSpacer("Entfernt Geschwindigkeitseinbußen beim Unterwasserabbau")
            }
        },
        rarity = Rarity.RARE,
    ),
    MULTISHOT(
        EnchantmentKeys.MULTISHOT,
        displayName = text("Multishot"),
        description = { _ ->
            line {
                darkSpacer("Verschießt 3 Pfeile anstelle von 1")
            }
        },
        rarity = Rarity.RARE,
    ),
    WIND_BURST(
        EnchantmentKeys.WIND_BURST,
        displayName = text("Wind Burst"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht den Rückstoß bei Schmetterangriffen um")
                    variableValue(" %placeholder%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to String.format("%.2f", 1.15 + 0.35 * level)
                )
            }
        ),
        rarity = Rarity.LEGENDARY,
    ),
    BLAST_PROTECTION(
        EnchantmentKeys.BLAST_PROTECTION,
        displayName = text("Blast Protection"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Reduziert eingehenden Explosionsschaden um")
                    variableValue(" %damage%% ")
                }
                line {
                    darkSpacer("und eingehenden Explosionsrückstoß um")
                    variableValue(" %knockback%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "damage" to (level * 8).toString(),
                    "knockback" to (level * 15).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    SWEEPING(
        EnchantmentKeys.SWEEPING_EDGE,
        displayName = text("Sweeping Edge"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht den Schaden von Schwungangriffen um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level.toDouble() / (level + 1) * 100).toInt().toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    PIERCING(
        EnchantmentKeys.PIERCING,
        displayName = text("Piercing"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Pfeile durchdringen bis zu")
                    variableValue(" %placeholder% ")
                    darkSpacer("Kreaturen")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level + 1).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    FIRE_PROTECTION(
        EnchantmentKeys.FIRE_PROTECTION,
        displayName = text("Fire Protection"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Reduziert den eingehenden Feuerschaden um")
                    variableValue(" %damage%% ")
                }
                line {
                    darkSpacer("und die Brenndauer um")
                    variableValue(" %time%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "damage" to (level * 8).toString(),
                    "time" to (level * 15).toString()
                )
            }
        ),
        rarity = Rarity.UNCOMMON,
    ),
    SWIFT_SNEAK(
        EnchantmentKeys.SWIFT_SNEAK,
        displayName = text("Swift Sneak"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert die Verlangsamung beim Kriechen um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to min((level * 15), 100).toString()
                )
            }
        ),
        rarity = Rarity.EPIC,
    ),
    MENDING(
        EnchantmentKeys.MENDING,
        displayName = text("Mending"),
        description = { _ ->
            line {
                darkSpacer("Repariere den Gegenstand, während du Erfahrung erhältst")
            }
        },
        rarity = Rarity.LEGENDARY,
    ),
    PROTECTION(
        EnchantmentKeys.PROTECTION,
        displayName = text("Protection"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Reduziert eingehenden Schaden um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 4).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    LURE(
        EnchantmentKeys.LURE,
        displayName = text("Lure"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert die Anbeißzeit von Fischen um")
                    variableValue(" %placeholder% ")
                    darkSpacer("Sekunden")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level * 5).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    PUNCH(
        EnchantmentKeys.PUNCH,
        displayName = text("Punch"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht den Pfeilrückstoß um")
                    variableValue(" %placeholder% ")
                    darkSpacer("Blöcke")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (3 * level).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    FROST_WALKER(
        EnchantmentKeys.FROST_WALKER,
        displayName = text("Frost Walker"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verwandelt Wasser innerhalb")

                }
                line {
                    darkSpacer("eines")
                    variableValue(" %placeholder% ")
                    darkSpacer("Blockradius des Spielers in Eis")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (level + 2).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    POWER(
        EnchantmentKeys.POWER,
        displayName = text("Power"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verleiht einen Bonus von")
                    variableValue(" %placeholder%% ")
                    darkSpacer("auf Pfeilschaden")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (25 * (level + 1)).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    RIPTIDE(
        EnchantmentKeys.RIPTIDE,
        displayName = text("Riptide"),
        description = { _ ->
            line {
                darkSpacer("Schleudert den Spieler beim Werfen")
            }
            line {
                darkSpacer("eines Dreizacks im Wasser oder im Regen nach vorne")
            }
        },
        rarity = Rarity.RARE,
    ),
    LUNGE(
        EnchantmentKeys.LUNGE,
        displayName = text("Lunge"),
        description = { _ ->
            line {
                darkSpacer("Schleudert den Spieler bei")
            }
            line {
                darkSpacer("Stoßangriffen eines Speers nach vorne")
            }
        },
        rarity = Rarity.RARE,
    ),
    BREACH(
        EnchantmentKeys.BREACH,
        displayName = text("Breach"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Reduziert die Wirksamkeit der Rüstung des Ziels um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (15 * level).toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    FORTUNE(
        EnchantmentKeys.FORTUNE,
        displayName = text("Fortune"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht Chance auf viele Block-Drops um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                val value = ceil(((1.0 / (level + 2)) + ((level + 1).toDouble() / 2)) * 100 - 100)
                object2ObjectMapOf(
                    "placeholder" to value.toInt().toString()
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    DENSITY(
        EnchantmentKeys.DENSITY,
        displayName = text("Density"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verursacht")
                    variableValue(" %placeholder% ")
                    darkSpacer("Bonusschaden pro gefallenem Block")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to (0.5 * level).toString()
                )
            }
        ),
        rarity = Rarity.COMMON,
    ),
    LOOTING(
        EnchantmentKeys.LOOTING,
        displayName = text("Looting"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Erhöht die maximale Anzahl an gewöhnlichen Drops um")
                    variableValue(" %common% ")
                }
                line {
                    darkSpacer("und die Chance, seltene Drops zu erhalten, um")
                    variableValue(" %rare% %")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "common" to level.toString(),
                    "rare" to level.toString()
                )
            }
        ),
        rarity = Rarity.EPIC,
    ),
    BINDING_CURSE(
        EnchantmentKeys.BINDING_CURSE,
        displayName = text("Curse of Binding"),
        description = { _ ->
            line {
                darkSpacer("Rüstung kann nicht mehr abgelegt werden")
            }
        },
        rarity = Rarity.EPIC,
    ),
    DEPTH_STRIDER(
        EnchantmentKeys.DEPTH_STRIDER,
        displayName = text("Depth Strider"),
        description = levelBased(
            block = {
                line {
                    darkSpacer("Verringert die Verlangsamung Unterwasser um")
                    variableValue(" %placeholder%%")
                }
            },
            placeholders = { level ->
                object2ObjectMapOf(
                    "placeholder" to String.format("%.2f", level * 33.333333)
                )
            }
        ),
        rarity = Rarity.RARE,
    ),
    VANISHING_CURSE(
        EnchantmentKeys.VANISHING_CURSE,
        displayName = text("Curse of Vanishing"),
        description = { _ ->
            line {
                darkSpacer("Beim Tod verschwindet der Gegenstand direkt")
            }
        },
        rarity = Rarity.EPIC,
    ),
    INFINITY(
        EnchantmentKeys.INFINITY,
        displayName = text("Infinity"),
        description = { _ ->
            line {
                darkSpacer("Verhindert, dass normale Pfeile beim Abschuss verbraucht werden")
            }
        },
        rarity = Rarity.EPIC,
    ),
    FLAME(
        EnchantmentKeys.FLAME,
        displayName = text("Flame"),
        description = { _ ->
            line {
                darkSpacer("Pfeile setzen dem Ziel")
                variableValue(" 5 ")
                darkSpacer("Feuerschaden")
            }
        },
        rarity = Rarity.RARE,
    );

    companion object {
        fun getByKey(key: Key) = entries.find { it.key.asString() == key.asString() }
    }
}

private fun levelBased(
    block: LoreBuilder.() -> Unit,
    placeholders: (Int) -> Object2ObjectMap<String, String>
): LoreBuilder.(Int) -> Unit = { level ->
    val placeholders = placeholders(level)
    val lines = LoreBuilder().apply {
        block()
    }.build()

    lines.forEach { line ->
        var newLine = line

        placeholders.forEach { (placeholder, value) ->
            newLine = newLine.replaceText {
                it.matchLiteral("%$placeholder%")
                it.replacement(value)
            }
        }

        line {
            append(newLine)
        }
    }
}
