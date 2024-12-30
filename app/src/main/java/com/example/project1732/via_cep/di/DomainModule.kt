
package com.example.project1732.via_cep.di


import com.example.project1732.via_cep.data.local.repository.AddressLocalRepositoryImpl
import com.example.project1732.via_cep.data.repository.AddressRepositoryImpl
import com.example.project1732.via_cep.domain.api.repository.AddressRepository
import com.example.project1732.via_cep.domain.local.repository.AddressLocalRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindsAddressRepositoryImpl(
        addressRepositoryImpl: AddressRepositoryImpl
    ): AddressRepository

    @Binds
    abstract fun bindsAddressLocalRepositoryImpl(
        addressLocalRepositoryImpl: AddressLocalRepositoryImpl
    ): AddressLocalRepository

}