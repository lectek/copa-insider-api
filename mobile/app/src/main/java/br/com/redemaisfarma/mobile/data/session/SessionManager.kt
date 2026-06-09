package br.com.redemaisfarma.mobile.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import br.com.redemaisfarma.mobile.data.model.LoginResponsePayload
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class AuthSession(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: Long?,
    val fullName: String?,
    val email: String?,
    val expiresAt: String?,
    val tenantId: String?
) {
    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank()
}

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("auth_session") }
        )

    val sessionFlow: Flow<AuthSession> = dataStore.data
        .catch { ex ->
            if (ex is IOException) {
                emit(emptyPreferences())
            } else {
                throw ex
            }
        }
        .map { pref ->
            AuthSession(
                accessToken = pref[KEY_ACCESS_TOKEN],
                refreshToken = pref[KEY_REFRESH_TOKEN],
                userId = pref[KEY_USER_ID],
                fullName = pref[KEY_FULL_NAME],
                email = pref[KEY_EMAIL],
                expiresAt = pref[KEY_EXPIRES_AT],
                tenantId = pref[KEY_TENANT_ID]
            )
        }

    val isAuthenticatedFlow: Flow<Boolean> = sessionFlow.map { it.isAuthenticated }

    suspend fun saveLogin(response: LoginResponsePayload) {
        dataStore.edit { pref ->
            putOrRemove(pref, KEY_ACCESS_TOKEN, response.accessToken)
            putOrRemove(pref, KEY_REFRESH_TOKEN, response.refreshToken)
            putOrRemove(pref, KEY_FULL_NAME, response.fullName)
            putOrRemove(pref, KEY_EMAIL, response.email)
            putOrRemove(pref, KEY_EXPIRES_AT, response.expiresAt)
            putOrRemove(pref, KEY_TENANT_ID, response.tenantId)
            if (response.userId == null) {
                pref.remove(KEY_USER_ID)
            } else {
                pref[KEY_USER_ID] = response.userId
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { pref ->
            pref.remove(KEY_ACCESS_TOKEN)
            pref.remove(KEY_REFRESH_TOKEN)
            pref.remove(KEY_USER_ID)
            pref.remove(KEY_FULL_NAME)
            pref.remove(KEY_EMAIL)
            pref.remove(KEY_EXPIRES_AT)
            pref.remove(KEY_TENANT_ID)
        }
    }

    suspend fun currentAccessToken(): String? = dataStore.data.first()[KEY_ACCESS_TOKEN]

    private fun putOrRemove(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<String>,
        value: String?
    ) {
        val safe = value?.trim()
        if (safe.isNullOrEmpty()) {
            pref.remove(key)
        } else {
            pref[key] = safe
        }
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_EXPIRES_AT = stringPreferencesKey("expires_at")
        val KEY_TENANT_ID = stringPreferencesKey("tenant_id")
    }
}
