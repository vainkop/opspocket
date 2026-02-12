package com.vainkop.opspocket.data.local

import platform.Foundation.NSUserDefaults

actual class SecureStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    actual fun hasKey(key: String): Boolean {
        return defaults.objectForKey(key) != null
    }

    actual fun getLong(key: String): Long {
        return defaults.integerForKey(key)
    }

    actual fun putLong(key: String, value: Long) {
        defaults.setInteger(value, forKey = key)
    }
}
