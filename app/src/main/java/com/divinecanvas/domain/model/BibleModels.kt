package com.divinecanvas.domain.model

/** Which testament a book belongs to — used to group the Book dropdown. */
enum class Testament {
    OT,
    NT
}

/** A book of the Bible with its canonical ordering and per-chapter verse counts. */
data class BibleBook(
    val order: Int,
    val name: String,
    val abbrev: String,
    val testament: Testament,
    /** verseCounts[i] = number of verses in chapter (i + 1). */
    val verseCounts: List<Int>,
) {
    val chapterCount: Int
        get() = verseCounts.size

    fun versesInChapter(chapter: Int): Int = verseCounts.getOrElse(chapter - 1) { 0 }
}

/** A fully-resolved verse with its display reference and text. */
data class Verse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val reference: String,
    val text: String,
    val translation: String,
)

/**
 * How a translation's text is sourced.
 * - [PUBLIC_DOMAIN] translations are free to bundle/serve. KJV ships as a complete offline asset;
 *   WEB/ASV come from the free key-less API and are cached.
 */
enum class TranslationTier {
    PUBLIC_DOMAIN
}

enum class Translation(
    val id: String,
    val displayName: String,
    val tier: TranslationTier,
    /** True when the full text is bundled in the APK and works with no network. */
    val bundledOffline: Boolean = false,
) {
    KJV("kjv", "KJV — King James Version", TranslationTier.PUBLIC_DOMAIN, bundledOffline = true),
    WEB("web", "WEB — World English Bible", TranslationTier.PUBLIC_DOMAIN),
    ASV("asv", "ASV — American Standard Version", TranslationTier.PUBLIC_DOMAIN);

    val isLicensed: Boolean
        get() = false

    companion object {
        fun fromId(id: String): Translation = entries.firstOrNull { it.id == id } ?: WEB
    }
}
