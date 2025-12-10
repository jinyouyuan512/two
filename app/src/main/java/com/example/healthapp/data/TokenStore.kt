package com.example.healthapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore by preferencesDataStore(name = "auth_tokens")

object TokenStore {
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")

    suspend fun save(context: Context, access: String?, refresh: String?, expiresAt: Long?) {
        context.tokenDataStore.edit { prefs ->
            if (access != null) prefs[KEY_ACCESS] = access else prefs.remove(KEY_ACCESS)
            if (refresh != null) prefs[KEY_REFRESH] = refresh else prefs.remove(KEY_REFRESH)
            if (expiresAt != null) prefs[KEY_EXPIRES_AT] = expiresAt else prefs.remove(KEY_EXPIRES_AT)
        }
    }

    suspend fun load(context: Context): Triple<String?, String?, Long?> {
        val flow = context.tokenDataStore.data.map { prefs ->
            Triple(prefs[KEY_ACCESS], prefs[KEY_REFRESH], prefs[KEY_EXPIRES_AT])
        }
        return flow.first()
    }

    suspend fun clear(context: Context) {
        context.tokenDataStore.edit { it.clear() }
    }
}

