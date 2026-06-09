package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.NotificacaoLidasRequest
import br.com.redemaisfarma.mobile.data.model.NotificacoesResponse
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject

class NotificacoesRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun listNotificacoes(): NotificacoesResponse = api.listNotificacoes()

    suspend fun marcarLidas(ids: List<Long>) {
        api.marcarNotificacoesLidas(NotificacaoLidasRequest(ids))
    }
}
