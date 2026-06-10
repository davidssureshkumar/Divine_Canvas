package com.divinecanvas.data.remote.api

import com.divinecanvas.data.remote.dto.BibleApiResponse
import com.divinecanvas.data.remote.dto.PexelsSearchResponse
import com.divinecanvas.data.remote.dto.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/** Free, key-less verse text service. Base url: https://bible-api.com/ */
interface BibleApi {
    @GET("{reference}")
    suspend fun getVerse(
        @Path("reference", encoded = false) reference: String,
        @Query("translation") translation: String = "web",
    ): BibleApiResponse
}

/** Optional Unsplash photo search. Base url: https://api.unsplash.com/ */
interface UnsplashApi {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Header("Authorization") clientAuth: String,
        @Query("query") query: String,
        @Query("orientation") orientation: String = "portrait",
        @Query("per_page") perPage: Int = 24,
    ): UnsplashSearchResponse
}

/** Optional Pexels photo search. Base url: https://api.pexels.com/v1/ */
interface PexelsApi {
    @GET("search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("orientation") orientation: String = "portrait",
        @Query("per_page") perPage: Int = 24,
    ): PexelsSearchResponse
}

