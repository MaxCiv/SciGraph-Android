package com.maxciv.scigraph.di

import com.maxciv.scigraph.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
@Suppress("unused")
@Module
abstract class AppModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity
}
