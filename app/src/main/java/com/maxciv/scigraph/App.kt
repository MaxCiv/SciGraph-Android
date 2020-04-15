package com.maxciv.scigraph

import com.maxciv.scigraph.di.DaggerAppComponent
import com.scichart.charting.visuals.SciChartSurface
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        SciChartSurface.setRuntimeLicenseKey(LICENSE_KEY)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().build()
    }
}
