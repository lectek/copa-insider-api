package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.ContentResolver
import android.net.Uri
import br.com.redemaisfarma.mobile.data.model.AvatarResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeUpdateRequest
import br.com.redemaisfarma.mobile.data.repository.ClienteRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ClienteRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<ClienteMeResponse>>(UiState.Idle)
    val state: StateFlow<UiState<ClienteMeResponse>> = _state.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<ClienteMeResponse>>(UiState.Idle)
    val updateState: StateFlow<UiState<ClienteMeResponse>> = _updateState.asStateFlow()

    private val _avatarState = MutableStateFlow<UiState<AvatarResponse>>(UiState.Idle)
    val avatarState: StateFlow<UiState<AvatarResponse>> = _avatarState.asStateFlow()

    init {
        carregarPerfil()
    }

    fun carregarPerfil() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val perfil = repository.getPerfil()
                _state.value = UiState.Success(perfil)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar perfil."))
            }
        }
    }

    fun atualizarPerfil(nome: String, email: String, cpf: String?, telefone: String?, endereco: String?) {
        _updateState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val request = ClienteMeUpdateRequest(
                    nome = nome,
                    email = email,
                    cpf = cpf,
                    telefone = telefone,
                    endereco = endereco
                )
                val atualizado = repository.updatePerfil(request)
                _updateState.value = UiState.Success(atualizado)
                _state.value = UiState.Success(atualizado)
            } catch (ex: Exception) {
                _updateState.value = UiState.Error(humanizeError(ex, "Falha ao atualizar perfil."))
            }
        }
    }

    fun uploadAvatar(contentResolver: ContentResolver, uri: Uri) {
        _avatarState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.uploadAvatar(contentResolver, uri)
                _avatarState.value = UiState.Success(response)
                carregarPerfil()
            } catch (ex: Exception) {
                _avatarState.value = UiState.Error(humanizeError(ex, "Falha ao enviar avatar."))
            }
        }
    }
}
