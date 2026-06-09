package br.com.redemaisfarma.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarRequest
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarResponse
import br.com.redemaisfarma.mobile.data.model.CheckoutResumoResponse
import br.com.redemaisfarma.mobile.data.repository.CheckoutRepository
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.humanizeError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: CheckoutRepository
) : ViewModel() {
    private val _resumoState = MutableStateFlow<UiState<CheckoutResumoResponse>>(UiState.Idle)
    val resumoState: StateFlow<UiState<CheckoutResumoResponse>> = _resumoState.asStateFlow()

    private val _finalizarState = MutableStateFlow<UiState<CheckoutFinalizarResponse>>(UiState.Idle)
    val finalizarState: StateFlow<UiState<CheckoutFinalizarResponse>> = _finalizarState.asStateFlow()

    init {
        carregarResumo()
    }

    fun carregarResumo() {
        _resumoState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resumo = repository.getResumo()
                _resumoState.value = UiState.Success(resumo)
            } catch (ex: Exception) {
                _resumoState.value = UiState.Error(humanizeError(ex, "Falha ao carregar resumo."))
            }
        }
    }

    fun finalizar(nome: String, cpf: String, email: String, pagamento: String) {
        _finalizarState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.finalizar(
                    CheckoutFinalizarRequest(nome = nome, cpf = cpf, email = email, pagamento = pagamento)
                )
                _finalizarState.value = UiState.Success(response)
            } catch (ex: Exception) {
                _finalizarState.value = UiState.Error(humanizeError(ex, "Falha ao finalizar checkout."))
            }
        }
    }
}
