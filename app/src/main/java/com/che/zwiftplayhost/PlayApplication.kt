package com.che.zwiftplayhost

import android.app.Application
import timber.log.Timber

class PlayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}