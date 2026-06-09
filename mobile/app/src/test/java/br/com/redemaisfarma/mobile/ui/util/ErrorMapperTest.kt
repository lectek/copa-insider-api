package br.com.redemaisfarma.mobile.ui.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMapperTest {
    @Test
    fun humanizeError_handlesHttpException() {
        val response = Response.error<String>(
            404,
            "Not found".toResponseBody("text/plain".toMediaType())
        )
        val ex = HttpException(response)
        assertEquals("Erro 404: Not Found", humanizeError(ex, "fallback"))
    }

    @Test
    fun humanizeError_handlesIoException() {
        val ex = IOException("timeout")
        assertEquals("Falha de conexao.", humanizeError(ex, "fallback"))
    }
}
