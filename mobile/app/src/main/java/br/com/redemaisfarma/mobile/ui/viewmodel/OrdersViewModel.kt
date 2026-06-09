package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.PedidoResumoResponse
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
class OrdersViewModel @Inject constructor(
    private val repository: PedidosRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<PedidoResumoResponse>>>(UiState.Idle)
    val state: StateFlow<UiState<List<PedidoResumoResponse>>> = _state.asStateFlow()

    init {
        carregarPedidos()
    }

    fun carregarPedidos() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val pedidos = repository.listPedidos()
                _state.value = UiState.Success(pedidos)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar pedidos."))
            }
        }
    }
}
