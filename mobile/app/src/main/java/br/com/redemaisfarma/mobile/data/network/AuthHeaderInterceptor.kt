package br.com.redemaisfarma.mobile.data.network

import br.com.redemaisfarma.mobile.data.session.SessionManager
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val current = chain.request()
        if (current.header("Authorization") != null) {
            return chain.proceed(current)
        }

        val token = runBlocking { sessionManager.currentAccessToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(current)
        }

        val authed = current.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authed)
    }
}
