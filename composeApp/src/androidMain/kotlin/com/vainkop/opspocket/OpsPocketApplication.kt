package com.vainkop.opspocket

import android.app.Application
import com.vainkop.opspocket.di.platformModule
import com.vainkop.opspocket.di.sharedModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class OpsPocketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@OpsPocketApplication)
            modules(platformModule + sharedModules)
        }
    }
}
