package com.example.project1732.via_cep.di

import com.example.project1732.via_cep.data.api.ServiceApi
import com.example.project1732.via_cep.network.ServiceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {

    @Provides
    @Singleton
    fun providesServiceApi(
        serviceProvider: ServiceProvider
    ): ServiceApi {
        return serviceProvider.createService(ServiceApi::class.java)
    }

}