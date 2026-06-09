package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.repository.FavoritosRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val repository: FavoritosRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Set<Long>>>(UiState.Idle)
    val state: StateFlow<UiState<Set<Long>>> = _state.asStateFlow()

    init {
        carregarFavoritos()
    }

    fun carregarFavoritos() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val ids = repository.listFavoritos().mapNotNull { it.produtoId }.toSet()
                _state.value = UiState.Success(ids)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao carregar favoritos."))
            }
        }
    }

    fun toggleFavorito(produtoId: Long, favorito: Boolean) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                if (favorito) {
                    repository.removeFavorito(produtoId)
                } else {
                    repository.addFavorito(produtoId)
                }
                val ids = repository.listFavoritos().mapNotNull { it.produtoId }.toSet()
                _state.value = UiState.Success(ids)
            } catch (ex: Exception) {
                _state.value = UiState.Error(humanizeError(ex, "Falha ao atualizar favorito."))
            }
        }
    }
}
