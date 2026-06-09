package br.com.redemaisfarma.mobile.data.repository

import android.content.ContentResolver
import android.net.Uri
import br.com.redemaisfarma.mobile.data.model.AvatarResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeResponse
import br.com.redemaisfarma.mobile.data.model.ClienteMeUpdateRequest
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ClienteRepository @Inject constructor(
    private val api: ClienteApiService
) {
    suspend fun getPerfil(): ClienteMeResponse = api.getPerfil()

    suspend fun updatePerfil(request: ClienteMeUpdateRequest): ClienteMeResponse =
        api.updatePerfil(request)

    suspend fun uploadAvatar(contentResolver: ContentResolver, uri: Uri): AvatarResponse {
        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Nao foi possivel ler a imagem.")
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
        return api.uploadAvatar(part)
    }
}
