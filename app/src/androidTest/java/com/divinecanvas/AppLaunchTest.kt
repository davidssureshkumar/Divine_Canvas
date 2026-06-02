package com.divinecanvas

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test: launch the real app (full Hilt graph + Compose UI) and confirm it reaches RESUMED
 * without crashing. The Editor is the start destination, so this exercises the exact path that
 * crashed during the Robo test (Hilt building the EditorViewModel -> repositories ->
 * KjvOfflineSource) on a real device.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {

    @Test
    fun appLaunchesWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
