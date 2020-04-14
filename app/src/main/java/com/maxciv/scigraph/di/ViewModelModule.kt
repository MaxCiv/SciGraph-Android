package com.maxciv.scigraph.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maxciv.scigraph.viewmodel.MainViewModel
import com.maxciv.scigraph.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(userViewModel: MainViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
