package com.vainkop.opspocket.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureApiKeyStorage @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        FILE_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, key).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
    }

    fun hasApiKey(): Boolean {
        return getApiKey() != null
    }

    private companion object {
        const val FILE_NAME = "opspocket_secure_prefs"
        const val KEY_API_KEY = "cast_ai_api_key"
    }
}
