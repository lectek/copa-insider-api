package br.com.redemaisfarma.mobile.data.network

import br.com.redemaisfarma.mobile.data.model.PageResponse
import br.com.redemaisfarma.mobile.data.model.ProdutoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProdutosPublicApiService {
    @GET("api/public/produtos")
    suspend fun listarProdutos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("q") query: String? = null,
        @Query("sort") sort: String = "nome",
        @Query("dir") dir: String = "asc"
    ): PageResponse<ProdutoResponse>

    @GET("api/public/produtos/destaques")
    suspend fun destaques(@Query("limit") limit: Int = 10): List<ProdutoResponse>

    @GET("api/public/produtos/{id}")
    suspend fun obter(@Path("id") id: Long): ProdutoResponse
}
