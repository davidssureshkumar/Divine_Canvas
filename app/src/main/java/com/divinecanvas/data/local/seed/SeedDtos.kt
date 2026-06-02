package com.divinecanvas.data.local.seed

import kotlinx.serialization.Serializable

@Serializable data class VersificationFile(val books: List<SeedBook>)

@Serializable
data class SeedBook(
    val order: Int,
    val name: String,
    val abbrev: String,
    val testament: String,
    val verses: List<Int>,
)

@Serializable data class ThemesFile(val translation: String, val themes: List<SeedTheme>)

@Serializable data class SeedTheme(val name: String, val verses: List<SeedThemeVerse>)

@Serializable
data class SeedThemeVerse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val reference: String,
    val text: String,
)
