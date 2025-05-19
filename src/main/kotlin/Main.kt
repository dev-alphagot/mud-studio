package io.github.devalphagot

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
}

fun main() {
    redefineChars()
    compile()
}