package com.maxciv.scigraph.di

import com.maxciv.scigraph.repository.DataRepository
import com.maxciv.scigraph.repository.RandomDataRepository
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
    fun provideDataRepository(): DataRepository {
        return RandomDataRepository()
    }
}
