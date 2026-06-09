package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.CartItemRequest
import br.com.redemaisfarma.mobile.data.model.CartSummaryResponse
import br.com.redemaisfarma.mobile.data.model.CartUpdateRequest
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun getCarrinho(): CartSummaryResponse = api.getCarrinho()

    suspend fun addItem(produtoId: Long, quantidade: Int): CartSummaryResponse =
        api.addCarrinho(CartItemRequest(produtoId, quantidade))

    suspend fun updateItem(produtoId: Long, quantidade: Int): CartSummaryResponse =
        api.updateCarrinho(produtoId, CartUpdateRequest(quantidade))

    suspend fun removeItem(produtoId: Long): CartSummaryResponse =
        api.removeCarrinho(produtoId)
}
