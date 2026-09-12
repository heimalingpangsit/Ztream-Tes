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
class NobarInviteViewModel @Inject constructor(
    private val repository: NobarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NobarInviteUiState())
    val uiState: StateFlow<NobarInviteUiState> = _uiState.asStateFlow()

    init {
        createRoom()
    }

    private fun createRoom() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRoom = true, errorMessage = null)
            try {
                val roomId = NobarRoomId.generate()
                val room = repository.createRoom(roomId)
                val friends = repository.getMutualFriends()
                _uiState.value = _uiState.value.copy(
                    isCreatingRoom = false,
                    room = room,
                    friends = friends,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingRoom = false,
                    errorMessage = e.message ?: "Gagal membuat room",
                )
            }
        }
    }

    fun invite(username: String) {
        val roomId = _uiState.value.room?.id ?: return
        if (_uiState.value.invitedUsernames.contains(username)) return

        viewModelScope.launch {
            try {
                repository.sendInvite(roomId, username)
                _uiState.value = _uiState.value.copy(
                    invitedUsernames = _uiState.value.invitedUsernames + username,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal mengundang teman",
                )
            }
        }
    }
}

data class NobarInviteUiState(
    val isCreatingRoom: Boolean = false,
    val room: NobarRoom? = null,
    val friends: List<NobarFriend> = emptyList(),
    val invitedUsernames: Set<String> = emptySet(),
    val errorMessage: String? = null,
)