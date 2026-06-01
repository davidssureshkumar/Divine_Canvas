package com.divinecanvas.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response shape from https://bible-api.com (free, no key required). */
@Serializable
data class BibleApiResponse(
    val reference: String = "",
    val verses: List<BibleApiVerse> = emptyList(),
    val text: String = "",
    @SerialName("translation_id") val translationId: String = "",
    @SerialName("translation_name") val translationName: String = "",
)

@Serializable
data class BibleApiVerse(
    @SerialName("book_name") val bookName: String = "",
    val chapter: Int = 0,
    val verse: Int = 0,
    val text: String = "",
)
