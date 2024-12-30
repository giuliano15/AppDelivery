//package com.example.viacep.data.api
package com.example.project1732.via_cep.data.api

import com.example.project1732.via_cep.data.model.AddressResponse

import retrofit2.http.GET
import retrofit2.http.Path

interface ServiceApi {

    @GET("{cep}/json")
    suspend fun getAddress(
        @Path("cep") cep: String
    ): AddressResponse

}