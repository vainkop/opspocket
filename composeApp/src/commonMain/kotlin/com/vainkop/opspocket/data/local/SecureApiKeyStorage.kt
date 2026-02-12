package com.vainkop.opspocket.data.local

class SecureApiKeyStorage(private val storage: SecureStorage) {

    fun saveApiKey(key: String) {
        storage.putString(KEY_API_KEY, key)
    }

    fun getApiKey(): String? {
        return storage.getString(KEY_API_KEY)
    }

    fun clearApiKey() {
        storage.remove(KEY_API_KEY)
    }

    fun hasApiKey(): Boolean {
        return storage.getString(KEY_API_KEY) != null
    }

    private companion object {
        const val KEY_API_KEY = "cast_ai_api_key"
    }
}
