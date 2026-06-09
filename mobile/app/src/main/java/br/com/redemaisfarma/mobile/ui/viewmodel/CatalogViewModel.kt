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
class CatalogViewModel @Inject constructor(
    private val repository: ProdutosRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ProdutoResponse>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ProdutoResponse>>> = _state.asStateFlow()
    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()
    private var currentPage = 0
    private var currentQuery: String? = null
    private var isLoadingMore = false

    init {
        carregarCatalogo()
    }

    fun carregarCatalogo(query: String? = null) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val safeQuery = query?.takeIf { it.isNotBlank() }
                currentQuery = safeQuery
                currentPage = 0
                val page = repository.listar(page = currentPage, query = safeQuery)
                _state.value = UiState.Success(page.content)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar catalogo."))
            }
        }
    }

    fun carregarMais() {
        if (isLoadingMore) return
        val current = state.value
        if (current !is UiState.Success) return
        isLoadingMore = true
        _loadingMore.value = true
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val page = repository.listar(page = nextPage, query = currentQuery)
                if (page.content.isNotEmpty()) {
                    currentPage = nextPage
                    val merged = current.data + page.content
                    _state.value = UiState.Success(merged)
                }
            } catch (_: Exception) {
                // Mantem estado atual em caso de falha no carregamento incremental.
            } finally {
                isLoadingMore = false
                _loadingMore.value = false
            }
        }
    }
}
