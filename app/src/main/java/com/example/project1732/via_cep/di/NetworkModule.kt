package com.example.project1732.via_cep.di


import com.example.project1732.via_cep.network.ServiceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun providesServiceProvider() = ServiceProvider

}