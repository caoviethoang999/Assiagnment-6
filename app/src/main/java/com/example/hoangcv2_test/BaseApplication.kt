package com.example.hoangcv2_test

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

//run application dagger hilt
@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}