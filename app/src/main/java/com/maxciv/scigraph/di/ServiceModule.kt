package com.maxciv.scigraph.di

import com.maxciv.scigraph.local.RandomDataService
import dagger.Module
import dagger.Provides

/**
 * @author maxim.oleynik
 * @since 16.04.2020
 */
@Module
class ServiceModule {

    @Provides
    fun provideRandomDataService(): RandomDataService {
        return RandomDataService()
    }
}
