package com.example.project1732.via_cep.domain.local.usecase


import com.example.project1732.via_cep.data.mapper.toEntity
import com.example.project1732.via_cep.domain.local.repository.AddressLocalRepository
import com.example.project1732.via_cep.domain.model.Address

import javax.inject.Inject

class InsertAddressUseCase @Inject constructor(
    private val repository: AddressLocalRepository
) {

    suspend operator fun invoke(address: Address): Long {
        return repository.insertAddress(address.toEntity())
    }

}