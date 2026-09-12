package com.zaaam.zreming.ui.nobar

import kotlin.random.Random

object NobarRoomId {

    const val PREFIX = "ZarStream-"

    private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(): String {
        val suffix = (1..6)
            .map { CHARS[Random.nextInt(CHARS.length)] }
            .joinToString("")
        return "$PREFIX$suffix"
    }

    fun normalize(input: String): String {
        val clean = input.trim().uppercase().removePrefix(PREFIX.uppercase())
        return "$PREFIX$clean"
    }

    fun isValid(code: String): Boolean {
        return Regex("^${Regex.escape(PREFIX)}[A-Z0-9]{6}$").matches(code)
    }
}