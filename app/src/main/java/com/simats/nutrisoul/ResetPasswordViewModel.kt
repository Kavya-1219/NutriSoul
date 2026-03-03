package com.simats.nutrisoul

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ResetStep { EMAIL, OTP, NEW_PASSWORD }

sealed class ResetUiState {
    data object Idle : ResetUiState()
    data object Loading : ResetUiState()
    data class Error(val message: String) : ResetUiState()
    data class Message(val text: String) : ResetUiState()
    data object Done : ResetUiState()
}

class ResetPasswordViewModel : ViewModel() {

    var step by mutableStateOf(ResetStep.EMAIL)
        private set

    var email by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    // token returned from backend after OTP verify
    var resetToken by mutableStateOf<String?>(null)
        private set

    var uiState by mutableStateOf<ResetUiState>(ResetUiState.Idle)
        private set

    // resend timer
    var resendSeconds by mutableStateOf(0)
        private set

    private var timerJob: Job? = null

    val isEmailValid: Boolean get() = Validation.isEmailValid(email)
    val isOtpValid: Boolean get() = otp.length == 6
    val isPasswordValid: Boolean get() = Validation.isPasswordValid(newPassword)
    val isPasswordMatch: Boolean get() = newPassword == confirmPassword

    fun onEmailChange(v: String) { email = v.trim() }
    fun onOtpChange(v: String) { otp = v.filter { it.isDigit() }.take(6) }
    fun onNewPasswordChange(v: String) { newPassword = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword = v }

    fun clearUiMessage() {
        if (uiState is ResetUiState.Message || uiState is ResetUiState.Error) {
            uiState = ResetUiState.Idle
        }
    }

    private fun startResendTimer(seconds: Int = 120) {
        timerJob?.cancel()
        resendSeconds = seconds
        timerJob = viewModelScope.launch {
            while (resendSeconds > 0) {
                delay(1000)
                resendSeconds -= 1
            }
        }
    }

    fun sendOtp() {
        if (!isEmailValid) {
            uiState = ResetUiState.Error("Please enter a valid email address.")
            return
        }
        uiState = ResetUiState.Loading

        // TODO BACKEND: Check if email is registered
        // if not registered -> { handleUserNotFound(); return }
        
        viewModelScope.launch {
            delay(1000) // Simulate network call
            uiState = ResetUiState.Message("OTP sent to $email")
            step = ResetStep.OTP
            startResendTimer(120)
        }
    }

    private fun notRegisteredMessage(): String =
        "We couldn’t find an account with this email. Please create an account to continue."

    fun handleUserNotFound() {
        uiState = ResetUiState.Error(notRegisteredMessage())
        step = ResetStep.EMAIL
    }

    fun resendOtp() {
        if (resendSeconds > 0) return
        sendOtp()
    }

    fun verifyOtp() {
        if (!isOtpValid) {
            uiState = ResetUiState.Error("Enter a valid 6-digit OTP.")
            return
        }
        uiState = ResetUiState.Loading

        viewModelScope.launch {
            delay(800) // Simulate network call
            resetToken = "TEMP_RESET_TOKEN"
            uiState = ResetUiState.Idle
            step = ResetStep.NEW_PASSWORD
        }
    }

    fun confirmReset() {
        if (!isPasswordValid) {
            uiState = ResetUiState.Error(Validation.passwordHint())
            return
        }
        if (!isPasswordMatch) {
            uiState = ResetUiState.Error("Passwords do not match.")
            return
        }
        val token = resetToken
        if (token.isNullOrBlank()) {
            uiState = ResetUiState.Error("Reset session expired. Please try again.")
            step = ResetStep.EMAIL
            return
        }

        uiState = ResetUiState.Loading
        viewModelScope.launch {
            delay(1200) // Simulate network call
            uiState = ResetUiState.Done
        }
    }

    fun back(navBack: () -> Unit) {
        clearUiMessage()
        when (step) {
            ResetStep.EMAIL -> navBack()
            ResetStep.OTP -> step = ResetStep.EMAIL
            ResetStep.NEW_PASSWORD -> step = ResetStep.OTP
        }
    }
}
