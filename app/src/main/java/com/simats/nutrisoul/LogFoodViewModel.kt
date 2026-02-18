package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ModalType {
    NONE,
    SCANNER,
    MANUAL_ENTRY,
    QUANTITY_SELECTOR
}

data class LogFoodUiState(
    val visibleModal: ModalType = ModalType.NONE
)

class LogFoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LogFoodUiState())
    val uiState: StateFlow<LogFoodUiState> = _uiState

    fun showModal(modalType: ModalType) {
        _uiState.value = _uiState.value.copy(visibleModal = modalType)
    }

    fun hideModal() {
        _uiState.value = _uiState.value.copy(visibleModal = ModalType.NONE)
    }
}