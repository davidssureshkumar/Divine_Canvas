package com.divinecanvas.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.divinecanvas.data.local.kjv.KjvOfflineSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a real Android runtime. This is the test that would have caught the ICU-regex launch
 * crash: constructing [KjvOfflineSource] compiles its regex in a static initializer (which the
 * desktop JVM tolerated but Android's ICU engine rejected). It also verifies the bundled KJV asset
 * parses and resolves correctly on-device, including chapters that were previously mis-versioned.
 */
@RunWith(AndroidJUnit4::class)
class KjvOfflineSourceInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val source =
        KjvOfflineSource(
            context = context,
            json =
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            ioDispatcher = Dispatchers.IO,
        )

    @Test
    fun resolvesKnownVersesOnDevice() = runBlocking {
        // John 3:16 (book order 43).
        val john = source.getText(bookOrder = 43, chapter = 3, verse = 16)
        assertNotNull("John 3:16 should resolve offline", john)
        assertTrue(john!!.contains("God so loved the world"))

        // Matthew 2:23 — exists in standard KJV (23 verses); the old dataset had 22.
        assertNotNull("Matthew 2:23 should resolve", source.getText(40, 2, 23))

        // Revelation 12 has 17 verses in standard KJV; v17 valid, v18 must not exist.
        assertNotNull("Revelation 12:17 should resolve", source.getText(66, 12, 17))
        assertEquals("Revelation 12 should have only 17 verses", null, source.getText(66, 12, 18))
    }
}
