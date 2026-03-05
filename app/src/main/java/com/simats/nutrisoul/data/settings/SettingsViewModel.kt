package com.simats.nutrisoul.data.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserSettingsState())
    val state = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val me = repository.loadMe()
                _state.update {
                    it.copy(
                        isLoading = false,
                        userName = me.name,
                        profileImageUrl = me.profile_image_url,
                        darkMode = me.dark_mode,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            // Optimistic update
            val oldMode = _state.value.darkMode
            _state.update { it.copy(darkMode = enabled) }
            try {
                repository.setDarkMode(enabled)
            } catch (e: Exception) {
                _state.update { it.copy(darkMode = oldMode, error = "Failed to update theme") }
            }
        }
    }

    fun uploadProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val response = repository.uploadProfileImage(body)
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        profileImageUrl = response.profile_image_url,
                        error = null
                    ) 
                }
                file.delete() // Clean up temp file
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Upload failed") }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.logout()
                onSuccess()
            } catch (e: Exception) {
                // Even if API fails, we often force local logout
                onSuccess()
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_profile_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
