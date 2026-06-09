package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.ProdutoResponse
import br.com.redemaisfarma.mobile.data.repository.ProdutosRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProdutosRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<ProdutoResponse>>(UiState.Idle)
    val state: StateFlow<UiState<ProdutoResponse>> = _state.asStateFlow()

    fun carregarProduto(id: Long) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val produto = repository.obter(id)
                _state.value = UiState.Success(produto)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar produto."))
            }
        }
    }
}
