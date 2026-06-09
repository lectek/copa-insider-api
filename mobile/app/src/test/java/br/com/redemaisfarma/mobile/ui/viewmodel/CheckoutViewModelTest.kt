package br.com.redemaisfarma.mobile.ui.viewmodel

import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarRequest
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarResponse
import br.com.redemaisfarma.mobile.data.model.CheckoutResumoResponse
import br.com.redemaisfarma.mobile.data.model.CartSummaryResponse
import br.com.redemaisfarma.mobile.data.model.PaymentMethodResponse
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import br.com.redemaisfarma.mobile.data.repository.CheckoutRepository
import br.com.redemaisfarma.mobile.testing.MainDispatcherRule
import br.com.redemaisfarma.mobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun carregarResumo_emiteSucesso() = runTest {
        val viewModel = CheckoutViewModel(CheckoutRepository(FakeClienteApiService()))
        viewModel.carregarResumo()
        advanceUntilIdle()
        val state = viewModel.resumoState.value
        assertTrue(state is UiState.Success)
    }

    @Test
    fun finalizar_emiteSucesso() = runTest {
        val viewModel = CheckoutViewModel(CheckoutRepository(FakeClienteApiService()))
        viewModel.finalizar("Cliente", "12345678901", "teste@exemplo.com", "pix")
        advanceUntilIdle()
        val state = viewModel.finalizarState.value
        assertTrue(state is UiState.Success)
    }
}

private class FakeClienteApiService : ClienteApiService {
    override suspend fun getPerfil() = throw UnsupportedOperationException()
    override suspend fun updatePerfil(request: br.com.redemaisfarma.mobile.data.model.ClienteMeUpdateRequest) =
        throw UnsupportedOperationException()
    override suspend fun uploadAvatar(file: okhttp3.MultipartBody.Part) =
        throw UnsupportedOperationException()
    override suspend fun listPedidos() = emptyList<br.com.redemaisfarma.mobile.data.model.PedidoResumoResponse>()
    override suspend fun getPedido(id: Long) =
        throw UnsupportedOperationException()
    override suspend fun getCarrinho() =
        CartSummaryResponse(items = emptyList(), subtotal = 0.0, total = 0.0, hasInvalidItems = false)
    override suspend fun addCarrinho(request: br.com.redemaisfarma.mobile.data.model.CartItemRequest) =
        getCarrinho()
    override suspend fun updateCarrinho(produtoId: Long, request: br.com.redemaisfarma.mobile.data.model.CartUpdateRequest) =
        getCarrinho()
    override suspend fun removeCarrinho(produtoId: Long) =
        getCarrinho()
    override suspend fun getCheckoutResumo(): CheckoutResumoResponse {
        return CheckoutResumoResponse(
            carrinho = getCarrinho(),
            metodosPagamento = listOf(PaymentMethodResponse("pix", "Pix", "PIX"))
        )
    }
    override suspend fun finalizarCheckout(request: CheckoutFinalizarRequest): CheckoutFinalizarResponse {
        return CheckoutFinalizarResponse(pedidoId = 10L, paymentMethodLabel = "Pix")
    }
    override suspend fun listFavoritos() = emptyList<br.com.redemaisfarma.mobile.data.model.FavoritoResponse>()
    override suspend fun addFavorito(request: br.com.redemaisfarma.mobile.data.model.FavoritoRequest) =
        throw UnsupportedOperationException()
    override suspend fun removeFavorito(produtoId: Long) =
        Unit
    override suspend fun listNotificacoes() =
        br.com.redemaisfarma.mobile.data.model.NotificacoesResponse(items = emptyList(), unreadCount = 0)
    override suspend fun marcarNotificacoesLidas(request: br.com.redemaisfarma.mobile.data.model.NotificacaoLidasRequest) =
        Unit
}
