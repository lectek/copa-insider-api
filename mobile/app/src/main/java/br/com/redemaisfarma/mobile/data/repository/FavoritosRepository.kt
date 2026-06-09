package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.FavoritoResponse
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject

class FavoritosRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun listFavoritos(): List<FavoritoResponse> = api.listFavoritos()

    suspend fun addFavorito(produtoId: Long) = api.addFavorito(br.com.redemaisfarma.mobile.data.model.FavoritoRequest(produtoId))

    suspend fun removeFavorito(produtoId: Long) = api.removeFavorito(produtoId)
}
