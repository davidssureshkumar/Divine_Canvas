package com.divinecanvas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per Bible book. [verseCountsCsv] stores the per-chapter verse counts as a
 * comma-separated string so the Verse dropdown can be populated entirely offline.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val order: Int,
    val name: String,
    val abbrev: String,
    val testament: String, // "OT" | "NT"
    val verseCountsCsv: String,
)

/**
 * Cached verse text, keyed by reference + translation. Seeded with the curated
 * theme verses (public-domain WEB) and populated with anything fetched online so
 * previously-viewed verses remain available offline.
 */
@Entity(
    tableName = "verses",
    indices = [Index(value = ["book", "chapter", "verse", "translation"], unique = true)],
)
data class VerseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val translation: String,
    val reference: String,
    val text: String,
)

/** A theme and one of its associated verse references (for the auto-pick mode). */
@Entity(
    tableName = "theme_verses",
    indices = [Index(value = ["theme"])],
)
data class ThemeVerseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val theme: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
)
