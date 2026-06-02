package com.divinecanvas.di

import android.content.Context
import androidx.room.Room
import com.divinecanvas.data.local.DivineCanvasDatabase
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.seed.BibleSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): DivineCanvasDatabase =
        Room.databaseBuilder(context, DivineCanvasDatabase::class.java, DivineCanvasDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBibleDao(db: DivineCanvasDatabase): BibleDao = db.bibleDao()

    @Provides
    @Singleton
    fun provideBibleSeeder(
        @ApplicationContext context: Context,
        json: Json,
    ): BibleSeeder = BibleSeeder(context, json)
}
