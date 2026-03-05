package com.simats.nutrisoul.data.network

import okhttp3.MultipartBody
import retrofit2.http.*

data class MeResponse(
    val name: String,
    val email: String,
    val dark_mode: Boolean,
    val profile_image_url: String?
)

data class UpdateSettingsRequest(
    val dark_mode: Boolean? = null
)

interface UserApi {

    @GET("api/me/")
    suspend fun getMe(): MeResponse

    @PATCH("api/me/settings/")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): MeResponse

    @Multipart
    @POST("api/me/profile-image/")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): MeResponse

    @POST("api/auth/logout/")
    suspend fun logout()
}
