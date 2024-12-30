package com.example.project1732.via_cep.domain.local.usecase


import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.domain.local.repository.AddressLocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAddressUseCase @Inject constructor(
    private val repository: AddressLocalRepository
) {

    operator fun invoke(): Flow<List<AddressEntity>> {
        return repository.getAllAddress()
    }

}