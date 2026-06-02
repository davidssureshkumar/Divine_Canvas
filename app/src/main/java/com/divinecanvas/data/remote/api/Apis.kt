package com.divinecanvas.data.remote.api

import com.divinecanvas.data.remote.dto.ApiBibleResponse
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

/**
 * Optional licensed-translation provider (NIV/NKJV/ESV via the user's own key). Base url:
 * https://api.scripture.api.bible/v1/ verseId format is "BOOK.CHAPTER.VERSE", e.g. "JHN.3.16".
 */
interface ApiBibleApi {
    @GET("bibles/{bibleId}/verses/{verseId}")
    suspend fun getVerse(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String,
        @Path("verseId") verseId: String,
        @Query("content-type") contentType: String = "text",
        @Query("include-notes") includeNotes: Boolean = false,
        @Query("include-titles") includeTitles: Boolean = false,
        @Query("include-chapter-numbers") includeChapterNumbers: Boolean = false,
        @Query("include-verse-numbers") includeVerseNumbers: Boolean = false,
    ): ApiBibleResponse
}
