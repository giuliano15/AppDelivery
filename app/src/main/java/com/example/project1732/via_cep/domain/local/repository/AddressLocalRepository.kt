
package com.example.project1732.via_cep.domain.local.repository


import com.example.project1732.via_cep.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

interface AddressLocalRepository {

    fun getAllAddress(): Flow<List<AddressEntity>>

    suspend fun getAddressById(id: Long): AddressEntity?

    suspend fun insertAddress(addressEntity: AddressEntity): Long

    suspend fun updateAddress(addressEntity: AddressEntity)

    suspend fun deleteAddress(addressEntity: AddressEntity)

}