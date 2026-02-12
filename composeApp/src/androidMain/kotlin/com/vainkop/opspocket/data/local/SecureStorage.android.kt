package com.vainkop.opspocket.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

actual class SecureStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "opspocket_secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    actual fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    actual fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    actual fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    actual fun hasKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    actual fun getLong(key: String): Long {
        return sharedPreferences.getLong(key, 0L)
    }

    actual fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
}
