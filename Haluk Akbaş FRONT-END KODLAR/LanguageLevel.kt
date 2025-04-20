package com.halukakbash.talk_app.data

enum class LanguageLevel(val level: String, val displayName: String) {
    A1("A1", "Beginner"),
    A2("A2", "Elementary"),
    B1("B1", "Intermediate"),
    B2("B2", "Upper Intermediate"),
    C1("C1", "Advanced"),
    C2("C2", "Mastery");

    companion object {
        fun fromString(level: String): LanguageLevel {
            return values().find { it.level == level } ?: A1
        }
    }
} 