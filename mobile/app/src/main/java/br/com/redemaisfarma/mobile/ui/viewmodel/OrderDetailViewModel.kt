package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.PedidoDetalheResponse
import br.com.redemaisfarma.mobile.data.repository.PedidosRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val repository: PedidosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<PedidoDetalheResponse>>(UiState.Idle)
    val state: StateFlow<UiState<PedidoDetalheResponse>> = _state.asStateFlow()

    init {
        val pedidoId = savedStateHandle.get<Long>("id") ?: 0L
        if (pedidoId > 0L) {
            carregarPedido(pedidoId)
        } else {
            _state.value = UiState.Error("Pedido invalido.")
        }
    }

    fun carregarPedido(id: Long) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val pedido = repository.getPedido(id)
                _state.value = UiState.Success(pedido)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar pedido."))
            }
        }
    }
}
