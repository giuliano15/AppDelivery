package com.example.project1732.via_cep.util

import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.domain.model.Address

interface OnAddressEditedListener {
    fun onAddressEdited(updatedAddress: Address)
    fun onAddressDeleted(updatedAddress: Address)
}
