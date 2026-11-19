package com.gmail.nthai.util

object PoiInitializer {
    @Volatile
    private var initialized = false

    fun init() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    try {
                        System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.NullLogger")
                    } catch (_: Exception) {
                    }
                    initialized = true
                }
            }
        }
    }
}
