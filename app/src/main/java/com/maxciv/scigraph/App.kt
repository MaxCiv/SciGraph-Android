package com.maxciv.scigraph

import android.app.Application
import timber.log.Timber

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
