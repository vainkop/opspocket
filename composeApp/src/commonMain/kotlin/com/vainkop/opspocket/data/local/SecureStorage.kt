package com.vainkop.opspocket.data.local

expect class SecureStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun hasKey(key: String): Boolean
    fun getLong(key: String): Long
    fun putLong(key: String, value: Long)
}
