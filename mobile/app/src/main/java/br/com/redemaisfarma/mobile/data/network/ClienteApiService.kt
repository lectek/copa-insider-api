package br.com.redemaisfarma.mobile.data.network

import br.com.redemaisfarma.mobile.data.model.AvatarResponse
import br.com.redemaisfarma.mobile.data.model.CartItemRequest
import br.com.redemaisfarma.mobile.data.model.CartSummaryResponse
import br.com.redemaisfarma.mobile.data.model.CartUpdateRequest
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarRequest
import br.com.redemaisfarma.mobile.data.model.CheckoutFinalizarResponse
import br.com.redemaisfarma.mobile.data.model.CheckoutResumoResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeUpdateRequest
import br.com.redemaisfarma.mobile.data.model.FavoritoRequest
import br.com.redemaisfarma.mobile.data.model.FavoritoResponse
import br.com.redemaisfarma.mobile.data.model.NotificacaoLidasRequest
import br.com.redemaisfarma.mobile.data.model.NotificacoesResponse
import br.com.redemaisfarma.mobile.data.model.PedidoDetalheResponse
import br.com.redemaisfarma.mobile.data.model.PedidoResumoResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ClienteApiService {
    @GET("api/cliente/me")
    suspend fun getPerfil(): ClienteMeResponse

    @PUT("api/cliente/me")
    suspend fun updatePerfil(@Body request: ClienteMeUpdateRequest): ClienteMeResponse

    @Multipart
    @POST("api/cliente/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): AvatarResponse

    @GET("api/cliente/me/pedidos")
    suspend fun listPedidos(): List<PedidoResumoResponse>

    @GET("api/cliente/me/pedidos/{id}")
    suspend fun getPedido(@Path("id") id: Long): PedidoDetalheResponse

    @GET("api/cliente/me/carrinho")
    suspend fun getCarrinho(): CartSummaryResponse

    @POST("api/cliente/me/carrinho")
    suspend fun addCarrinho(@Body request: CartItemRequest): CartSummaryResponse

    @PUT("api/cliente/me/carrinho/{produtoId}")
    suspend fun updateCarrinho(
        @Path("produtoId") produtoId: Long,
        @Body request: CartUpdateRequest
    ): CartSummaryResponse

    @DELETE("api/cliente/me/carrinho/{produtoId}")
    suspend fun removeCarrinho(@Path("produtoId") produtoId: Long): CartSummaryResponse

    @GET("api/cliente/me/checkout/resumo")
    suspend fun getCheckoutResumo(): CheckoutResumoResponse

    @POST("api/cliente/me/checkout/finalizar")
    suspend fun finalizarCheckout(@Body request: CheckoutFinalizarRequest): CheckoutFinalizarResponse

    @GET("api/cliente/me/favoritos")
    suspend fun listFavoritos(): List<FavoritoResponse>

    @POST("api/cliente/me/favoritos")
    suspend fun addFavorito(@Body request: FavoritoRequest): FavoritoResponse

    @DELETE("api/cliente/me/favoritos/{produtoId}")
    suspend fun removeFavorito(@Path("produtoId") produtoId: Long)

    @GET("api/cliente/me/notificacoes")
    suspend fun listNotificacoes(): NotificacoesResponse

    @POST("api/cliente/me/notificacoes/lidas")
    suspend fun marcarNotificacoesLidas(@Body request: NotificacaoLidasRequest)
}
