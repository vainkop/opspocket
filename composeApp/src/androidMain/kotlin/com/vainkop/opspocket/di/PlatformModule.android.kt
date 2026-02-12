package com.vainkop.opspocket.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.vainkop.opspocket.data.local.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { SecureStorage(androidContext()) }
    single<Settings> { SharedPreferencesSettings.Factory(androidContext()).create("azure_preferences") }
}
