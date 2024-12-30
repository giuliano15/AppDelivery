package com.example.project1732.via_cep.domain.api.repository

import com.example.project1732.via_cep.data.model.AddressResponse


interface AddressRepository {

    suspend fun getAddress(cep: String): AddressResponse

}