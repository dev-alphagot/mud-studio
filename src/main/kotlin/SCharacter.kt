@file:Suppress("ClassName", "PropertyName", "SpellCheckingInspection")

package io.github.devalphagot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@Serializable
enum class ConsoleColor {
    BLACK, DARKBLUE, DARKGREEN,
    DARKCYAN, DARKRED, DARKMAGENTA, DARKYELLOW,
    GRAY, DARKGRAY, BLUE, GREEN, CYAN,
    RED, MAGENTA, YELLOW, WHITE, APRICOT,
    INDIGO_M_A400, BLUE_CR, YELLOW_M_700
}

@Serializable
data class _Char(
    val id: Int,
    val _name: String,
    val locator: String,
    val color: ConsoleColor
)

val chars = mutableMapOf<String, _Char>()

fun redefineChars(){
    json.decodeFromStream<List<_Char>>(File("workspace/characters.json").inputStream()).forEach {
        chars[it.locator] = it
    }

    chars.forEach { c ->
        File("compiled/char/${c.key}.txt").writeText("""
            ${c.value.id}
            ${c.value.color.ordinal}
        """.trimIndent().trim().split("\n").joinToString("\n") { it.trim() })
    }
    File("compiled/char_index.txt").writeText(chars.values.sortedBy { it.id }.joinToString("\n") { it.locator })

    println("CHAR COMPLETED")
}
