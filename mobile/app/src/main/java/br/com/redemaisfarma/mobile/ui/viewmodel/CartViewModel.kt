package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.CartSummaryResponse
import br.com.redemaisfarma.mobile.data.repository.CartRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<CartSummaryResponse>>(UiState.Idle)
    val state: StateFlow<UiState<CartSummaryResponse>> = _state.asStateFlow()

    init {
        carregarCarrinho()
    }

    fun carregarCarrinho() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val summary = repository.getCarrinho()
                _state.value = UiState.Success(summary)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar carrinho."))
            }
        }
    }

    fun removerItem(produtoId: Long) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val summary = repository.removeItem(produtoId)
                _state.value = UiState.Success(summary)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao remover item."))
            }
        }
    }

    fun adicionarItem(produtoId: Long, quantidade: Int) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val summary = repository.addItem(produtoId, quantidade)
                _state.value = UiState.Success(summary)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao adicionar item."))
            }
        }
    }
}
