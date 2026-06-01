package com.divinecanvas.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Unsplash ---
@Serializable
data class UnsplashSearchResponse(val results: List<UnsplashPhoto> = emptyList())

@Serializable
data class UnsplashPhoto(
    val id: String = "",
    val urls: UnsplashUrls = UnsplashUrls(),
    val user: UnsplashUser = UnsplashUser(),
)

@Serializable
data class UnsplashUrls(
    val regular: String = "",
    val small: String = "",
)

@Serializable
data class UnsplashUser(val name: String = "")

// --- Pexels ---
@Serializable
data class PexelsSearchResponse(val photos: List<PexelsPhoto> = emptyList())

@Serializable
data class PexelsPhoto(
    val id: Long = 0,
    val photographer: String = "",
    val src: PexelsSrc = PexelsSrc(),
)

@Serializable
data class PexelsSrc(
    val large: String = "",
    @SerialName("medium") val medium: String = "",
)
