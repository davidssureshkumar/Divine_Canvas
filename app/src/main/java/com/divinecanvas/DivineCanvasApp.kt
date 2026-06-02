package com.divinecanvas

import android.app.Application
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.seed.BibleSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DivineCanvasApp : Application() {

    @Inject lateinit var seeder: BibleSeeder
    @Inject lateinit var bibleDao: BibleDao

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Pre-populate the offline Bible database on first launch.
        appScope.launch { runCatching { seeder.seedIfNeeded(bibleDao) } }
    }
}
