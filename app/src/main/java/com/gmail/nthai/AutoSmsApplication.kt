package com.gmail.nthai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AutoSmsApplication : Application(), Configuration.Provider {

    companion object {
        init {
            // Force Apache POI to use NullLogger on Android to avoid Log4j2 initialization crash
            System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.NullLogger")
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
