package br.com.redemaisfarma.mobile.data.network

import br.com.redemaisfarma.mobile.data.model.LoginRequestPayload
import br.com.redemaisfarma.mobile.data.model.LoginResponsePayload
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestPayload): LoginResponsePayload
}
