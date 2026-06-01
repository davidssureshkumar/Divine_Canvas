package com.divinecanvas.data.repository

import com.divinecanvas.BuildConfig
import com.divinecanvas.core.AppResult
import com.divinecanvas.data.remote.api.PexelsApi
import com.divinecanvas.data.remote.api.UnsplashApi
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.DefaultBackgrounds
import com.divinecanvas.di.IoDispatcher
import com.divinecanvas.domain.repository.BackgroundRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundRepositoryImpl @Inject constructor(
    private val unsplashApi: UnsplashApi,
    private val pexelsApi: PexelsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BackgroundRepository {

    private val unsplashKey = BuildConfig.UNSPLASH_ACCESS_KEY
    private val pexelsKey = BuildConfig.PEXELS_API_KEY

    override val photoSearchAvailable: Boolean
        get() = unsplashKey.isNotBlank() || pexelsKey.isNotBlank()

    override fun gradients(): List<CanvasBackground.Gradient> = DefaultBackgrounds.all()

    override suspend fun searchPhotos(query: String): AppResult<List<CanvasBackground.Photo>> =
        withContext(ioDispatcher) {
            if (!photoSearchAvailable) {
                return@withContext AppResult.Failure("No photo provider configured")
            }
            try {
                val photos = when {
                    unsplashKey.isNotBlank() -> unsplashApi
                        .searchPhotos("Client-ID $unsplashKey", query)
                        .results.map {
                            CanvasBackground.Photo(
                                id = "unsplash_${it.id}",
                                url = it.urls.regular,
                                thumbnailUrl = it.urls.small,
                                attribution = "Photo by ${it.user.name} on Unsplash",
                            )
                        }

                    else -> pexelsApi
                        .searchPhotos(pexelsKey, query)
                        .photos.map {
                            CanvasBackground.Photo(
                                id = "pexels_${it.id}",
                                url = it.src.large,
                                thumbnailUrl = it.src.medium,
                                attribution = "Photo by ${it.photographer} on Pexels",
                            )
                        }
                }
                AppResult.Success(photos)
            } catch (e: Exception) {
                AppResult.Failure(e.message ?: "Photo search failed", e)
            }
        }
}
