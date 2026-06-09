package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.PedidoDetalheResponse
import br.com.redemaisfarma.mobile.data.model.PedidoResumoResponse
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject

class PedidosRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun listPedidos(): List<PedidoResumoResponse> = api.listPedidos()

    suspend fun getPedido(id: Long): PedidoDetalheResponse = api.getPedido(id)
}
