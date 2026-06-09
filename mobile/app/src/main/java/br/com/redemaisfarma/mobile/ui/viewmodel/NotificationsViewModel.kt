package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.NotificacaoResponse
import br.com.redemaisfarma.mobile.data.repository.NotificacoesRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificacoesRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<NotificacaoResponse>>>(UiState.Idle)
    val state: StateFlow<UiState<List<NotificacaoResponse>>> = _state.asStateFlow()

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resposta = repository.listNotificacoes()
                _state.value = UiState.Success(resposta.items)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar avisos."))
            }
        }
    }

    fun marcarTodasComoLidas(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.marcarLidas(ids)
                carregarNotificacoes()
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao marcar avisos."))
            }
        }
    }
}
