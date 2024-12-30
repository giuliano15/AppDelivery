package com.example.project1732.via_cep.data.repository


import com.example.project1732.via_cep.data.api.ServiceApi
import com.example.project1732.via_cep.data.model.AddressResponse
import com.example.project1732.via_cep.domain.api.repository.AddressRepository


import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val service: ServiceApi
) : AddressRepository {

    override suspend fun getAddress(cep: String): AddressResponse {
        return service.getAddress(cep)
    }
}