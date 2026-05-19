package com.example.woosh

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WooshApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.woosh.data.remote.RetrofitClient.init(this)
    }
}
