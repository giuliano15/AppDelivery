package com.example.project1732.via_cep.domain.usecase

import com.example.project1732.via_cep.data.mapper.toDomain
import com.example.project1732.via_cep.domain.api.repository.AddressRepository
import com.example.project1732.via_cep.domain.model.Address
import javax.inject.Inject

class GetAddressUseCase @Inject constructor(
    private val repository: AddressRepository
) {

    suspend operator fun invoke(cep: String): Address {
        return repository.getAddress(cep).toDomain()
    }

}