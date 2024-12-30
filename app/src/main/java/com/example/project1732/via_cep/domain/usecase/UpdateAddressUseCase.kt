package com.example.project1732.via_cep.domain.usecase

import com.example.project1732.via_cep.data.local.dao.AddressDao
import com.example.project1732.via_cep.data.local.entity.AddressEntity
import javax.inject.Inject

class UpdateAddressUseCase @Inject constructor(
    private val addressDao: AddressDao
) {
    suspend operator fun invoke(addressEntity: AddressEntity) {
        addressDao.updateAddress(addressEntity)
    }
}
