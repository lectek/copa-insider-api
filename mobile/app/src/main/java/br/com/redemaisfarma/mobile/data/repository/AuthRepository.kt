package br.com.redemaisfarma.mobile.data.repository

import br.com.redemaisfarma.mobile.data.model.LoginRequestPayload
import br.com.redemaisfarma.mobile.data.model.LoginResponsePayload
import br.com.redemaisfarma.mobile.data.network.AuthApiService
import br.com.redemaisfarma.mobile.data.session.SessionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val sessionManager: SessionManager
) {
    val isAuthenticatedFlow: Flow<Boolean> = sessionManager.isAuthenticatedFlow

    suspend fun login(usuario: String, senha: String): LoginResponsePayload {
        val payload = api.login(
            LoginRequestPayload(
                usuario = usuario.trim(),
                senha = senha,
                lembrarMe = true,
                tenantId = "rede-mais-farma"
            )
        )
        sessionManager.saveLogin(payload)
        return payload
    }

    suspend fun logout() {
        sessionManager.clear()
    }
}
