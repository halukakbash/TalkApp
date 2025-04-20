package com.halukakbash.talk_app.data

data class User(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val age: Int = 0,
    val country: String = "",
    val gender: String = "",
    val languageLevel: String = "",
    val nativeLanguage: String = "",
    val profilePhotoUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0,
    val rating: Int = 100,
    val talks: Int = 0
) {
    @get:JvmName("getLanguageLevelEnum")
    val languageLevelEnum: LanguageLevel
        get() = LanguageLevel.fromString(languageLevel)
} 