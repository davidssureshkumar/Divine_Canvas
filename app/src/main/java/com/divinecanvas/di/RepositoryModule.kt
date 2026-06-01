package com.divinecanvas.di

import com.divinecanvas.data.repository.BackgroundRepositoryImpl
import com.divinecanvas.data.repository.BibleRepositoryImpl
import com.divinecanvas.domain.repository.BackgroundRepository
import com.divinecanvas.domain.repository.BibleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBibleRepository(impl: BibleRepositoryImpl): BibleRepository

    @Binds
    @Singleton
    abstract fun bindBackgroundRepository(impl: BackgroundRepositoryImpl): BackgroundRepository
}
