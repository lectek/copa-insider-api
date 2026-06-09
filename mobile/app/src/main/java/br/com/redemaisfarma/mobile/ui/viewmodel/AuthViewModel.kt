package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.repository.AuthRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.isAuthenticatedFlow.collect { authenticated ->
                _isAuthenticated.value = authenticated
            }
        }
    }

    fun login(usuario: String, senha: String) {
        val usuarioTrim = usuario.trim()
        if (usuarioTrim.isBlank() || senha.isBlank()) {
            _loginState.value = UiState.Error("Informe usuario e senha.")
            return
        }

        _loginState.value = UiState.Loading
        viewModelScope.launch {
            try {
                repository.login(usuarioTrim, senha)
                _loginState.value = UiState.Success(Unit)
            } catch (ex: Exception) {
                _loginState.value = UiState.Error(
                    humanizeError(ex, "Falha ao autenticar.")
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = UiState.Idle
        }
    }
}
