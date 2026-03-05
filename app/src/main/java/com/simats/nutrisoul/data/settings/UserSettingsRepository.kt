package com.simats.nutrisoul.data.settings

import com.simats.nutrisoul.data.network.UpdateSettingsRequest
import com.simats.nutrisoul.data.network.UserApi
import okhttp3.MultipartBody
import javax.inject.Inject

class UserSettingsRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun loadMe() = api.getMe()

    suspend fun setDarkMode(enabled: Boolean) =
        api.updateSettings(UpdateSettingsRequest(dark_mode = enabled))

    suspend fun uploadProfileImage(part: MultipartBody.Part) =
        api.uploadProfileImage(part)

    suspend fun logout() = api.logout()
}
