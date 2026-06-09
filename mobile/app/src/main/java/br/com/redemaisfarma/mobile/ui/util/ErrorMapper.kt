package br.com.redemaisfarma.mobile.ui.util

import java.io.IOException
import retrofit2.HttpException

fun humanizeError(ex: Throwable, fallback: String): String {
    return when (ex) {
        is HttpException -> {
            val message = ex.message()
            if (message.isNullOrBlank()) "Erro ${ex.code()}" else "Erro ${ex.code()}: $message"
        }
        is IOException -> "Falha de conexao."
        else -> ex.message ?: fallback
    }
}
