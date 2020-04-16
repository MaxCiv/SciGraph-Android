package com.maxciv.scigraph.di

import com.maxciv.scigraph.local.RandomDataService
import com.maxciv.scigraph.repository.DataRepository
import com.maxciv.scigraph.repository.LocalDataRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author maxim.oleynik
 * @since 15.04.2020
 */
@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideDataRepository(randomDataService: RandomDataService): DataRepository {
        return LocalDataRepository(randomDataService)
    }
}
