package com.zaaam.zreming.ui.nobar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NobarJoinViewModel @Inject constructor(
    private val repository: NobarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NobarJoinUiState())
    val uiState: StateFlow<NobarJoinUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value, errorMessage = null)
    }

    fun join(onSuccess: (String) -> Unit) {
        val current = _uiState.value
        val roomId = NobarRoomId.normalize(current.input)

        if (!NobarRoomId.isValid(roomId)) {
            _uiState.value = current.copy(errorMessage = "Format kode tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isJoining = true, errorMessage = null)
            try {
                val exists = repository.roomExists(roomId)
                if (!exists) {
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        errorMessage = "Room tidak ditemukan",
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(isJoining = false)
                onSuccess(roomId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isJoining = false,
                    errorMessage = e.message ?: "Gagal join room",
                )
            }
        }
    }
}

data class NobarJoinUiState(
    val input: String = "",
    val isJoining: Boolean = false,
    val errorMessage: String? = null,
)