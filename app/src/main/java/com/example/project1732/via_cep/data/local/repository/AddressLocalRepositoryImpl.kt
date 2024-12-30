package com.example.project1732.via_cep.data.local.repository

import com.example.project1732.via_cep.data.local.dao.AddressDao
import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.domain.local.repository.AddressLocalRepository

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddressLocalRepositoryImpl @Inject constructor(
    private val addressDao: AddressDao
): AddressLocalRepository {

    override fun getAllAddress(): Flow<List<AddressEntity>> {
        return addressDao.getAllAddress()
    }

    override suspend fun getAddressById(id: Long): AddressEntity? {
        return getAddressById(id)
    }

    override suspend fun insertAddress(addressEntity: AddressEntity): Long {
        return addressDao.insertAddress(addressEntity)
    }

    override suspend fun updateAddress(addressEntity: AddressEntity) {
        addressDao.updateAddress(addressEntity)
    }

    override suspend fun deleteAddress(addressEntity: AddressEntity) {
        addressDao.deleteAddress(addressEntity)
    }
}