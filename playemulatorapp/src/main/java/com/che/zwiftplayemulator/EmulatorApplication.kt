package com.che.zwiftplayemulator

import android.app.Application
import timber.log.Timber

class EmulatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}