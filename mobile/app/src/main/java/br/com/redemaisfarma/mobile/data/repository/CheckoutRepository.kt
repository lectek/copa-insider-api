package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarRequest
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarResponse
import br.com.redemaisfarma.mobile.data.model.CheckoutResumoResponse
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject

class CheckoutRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun getResumo(): CheckoutResumoResponse = api.getCheckoutResumo()

    suspend fun finalizar(request: CheckoutFinalizarRequest): CheckoutFinalizarResponse =
        api.finalizarCheckout(request)
}
