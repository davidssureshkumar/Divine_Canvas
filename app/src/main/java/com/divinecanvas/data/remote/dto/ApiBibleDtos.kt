package com.divinecanvas.data.remote.dto

import kotlinx.serialization.Serializable

/** Response shape from scripture.api.bible verse endpoint. */
@Serializable
data class ApiBibleResponse(val data: ApiBibleData = ApiBibleData())

@Serializable
data class ApiBibleData(
    val id: String = "",
    val reference: String = "",
    val content: String = "",
)
