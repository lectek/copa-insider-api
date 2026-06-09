package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.PageResponse
import br.com.redemaisfarma.mobile.data.model.ProdutoResponse
import br.com.redemaisfarma.mobile.data.network.ProdutosPublicApiService
import javax.inject.Inject

class ProdutosRepository @Inject constructor(
    private val api: ProdutosPublicApiService
) {
    suspend fun listar(page: Int = 0, size: Int = 20, query: String? = null): PageResponse<ProdutoResponse> =
        api.listarProdutos(page = page, size = size, query = query)

    suspend fun obter(id: Long): ProdutoResponse = api.obter(id)
}
