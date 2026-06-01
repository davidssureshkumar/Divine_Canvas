package com.divinecanvas.data

import com.divinecanvas.core.AppResult
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.entity.VerseEntity
import com.divinecanvas.data.local.kjv.KjvOfflineSource
import com.divinecanvas.data.remote.api.ApiBibleApi
import com.divinecanvas.data.remote.api.BibleApi
import com.divinecanvas.data.remote.dto.BibleApiResponse
import com.divinecanvas.data.repository.BibleRepositoryImpl
import com.divinecanvas.domain.model.Translation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleRepositoryImplTest {

    private val dao = mockk<BibleDao>(relaxed = true)
    private val api = mockk<BibleApi>()
    private val apiBibleApi = mockk<ApiBibleApi>(relaxed = true)
    private val kjvSource = mockk<KjvOfflineSource>(relaxed = true)
    private val repo = BibleRepositoryImpl(dao, api, apiBibleApi, kjvSource, UnconfinedTestDispatcher())

    @Test
    fun `cache hit returns immediately without hitting the network`() = runTest {
        coEvery { dao.getCachedVerse("John", 3, 16, "web") } returns VerseEntity(
            book = "John", chapter = 3, verse = 16, translation = "web",
            reference = "John 3:16", text = "For God so loved the world…",
        )

        val result = repo.getVerse("John", 3, 16, Translation.WEB)

        assertTrue(result is AppResult.Success)
        assertEquals("For God so loved the world…", (result as AppResult.Success).data.text)
        coVerify(exactly = 0) { api.getVerse(any(), any()) }
    }

    @Test
    fun `cache miss fetches from api and caches the result`() = runTest {
        coEvery { dao.getCachedVerse(any(), any(), any(), any()) } returns null
        coEvery { api.getVerse(any(), any()) } returns BibleApiResponse(
            reference = "Psalms 23:1",
            text = "Yahweh is my shepherd; I shall lack nothing.",
            translationId = "web",
        )

        val result = repo.getVerse("Psalms", 23, 1, Translation.WEB)

        assertTrue(result is AppResult.Success)
        assertFalse((result as AppResult.Success).fromCache)
        coVerify(exactly = 1) { dao.cacheVerse(any()) }
    }

    @Test
    fun `network failure with no cache surfaces a failure`() = runTest {
        coEvery { dao.getCachedVerse(any(), any(), any(), any()) } returns null
        coEvery { api.getVerse(any(), any()) } throws RuntimeException("offline")

        val result = repo.getVerse("Obadiah", 1, 1, Translation.KJV)

        assertTrue(result is AppResult.Failure)
    }
}
