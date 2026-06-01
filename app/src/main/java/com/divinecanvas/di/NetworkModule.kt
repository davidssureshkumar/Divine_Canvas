package com.divinecanvas.di

import com.divinecanvas.BuildConfig
import com.divinecanvas.data.remote.api.ApiBibleApi
import com.divinecanvas.data.remote.api.BibleApi
import com.divinecanvas.data.remote.api.PexelsApi
import com.divinecanvas.data.remote.api.UnsplashApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BibleRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class UnsplashRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class PexelsRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class ApiBibleRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BIBLE_BASE = "https://bible-api.com/"
    private const val UNSPLASH_BASE = "https://api.unsplash.com/"
    private const val PEXELS_BASE = "https://api.pexels.com/v1/"
    private const val API_BIBLE_BASE = "https://api.scripture.api.bible/v1/"

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides @Singleton @BibleRetrofit
    fun provideBibleRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(BIBLE_BASE, client, json)

    @Provides @Singleton @UnsplashRetrofit
    fun provideUnsplashRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(UNSPLASH_BASE, client, json)

    @Provides @Singleton @PexelsRetrofit
    fun providePexelsRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(PEXELS_BASE, client, json)

    @Provides @Singleton @ApiBibleRetrofit
    fun provideApiBibleRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(API_BIBLE_BASE, client, json)

    @Provides @Singleton
    fun provideBibleApi(@BibleRetrofit retrofit: Retrofit): BibleApi =
        retrofit.create(BibleApi::class.java)

    @Provides @Singleton
    fun provideUnsplashApi(@UnsplashRetrofit retrofit: Retrofit): UnsplashApi =
        retrofit.create(UnsplashApi::class.java)

    @Provides @Singleton
    fun providePexelsApi(@PexelsRetrofit retrofit: Retrofit): PexelsApi =
        retrofit.create(PexelsApi::class.java)

    @Provides @Singleton
    fun provideApiBibleApi(@ApiBibleRetrofit retrofit: Retrofit): ApiBibleApi =
        retrofit.create(ApiBibleApi::class.java)
}
