package com.divinecanvas.domain

import com.divinecanvas.domain.model.BannerConfig
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.Testament
import com.divinecanvas.domain.model.Translation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleBookTest {

    private val genesis = BibleBook(
        order = 1,
        name = "Genesis",
        abbrev = "GEN",
        testament = Testament.OT,
        verseCounts = listOf(31, 25, 24, 26, 32), // first 5 chapters
    )

    @Test
    fun `chapter count reflects versification size`() {
        assertEquals(5, genesis.chapterCount)
    }

    @Test
    fun `verses in chapter returns correct count`() {
        assertEquals(31, genesis.versesInChapter(1))
        assertEquals(32, genesis.versesInChapter(5))
    }

    @Test
    fun `out of range chapter returns zero`() {
        assertEquals(0, genesis.versesInChapter(99))
        assertEquals(0, genesis.versesInChapter(0))
    }

    @Test
    fun `banner is disabled when text is blank`() {
        assertFalse(BannerConfig(text = "   ").enabled)
        assertFalse(BannerConfig(text = "").enabled)
        assertTrue(BannerConfig(text = "@grace").enabled)
    }

    @Test
    fun `translation falls back to WEB for unknown id`() {
        assertEquals(Translation.WEB, Translation.fromId("nope"))
        assertEquals(Translation.KJV, Translation.fromId("kjv"))
    }
}
