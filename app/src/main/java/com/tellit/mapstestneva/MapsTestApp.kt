package com.tellit.mapstestneva

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MapsTestApp : Application() {
    companion object {
        lateinit var INSTANCE: MapsTestApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}