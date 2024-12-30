package com.example.project1732.via_cep.data.mapper

import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.data.model.AddressResponse
import com.example.project1732.via_cep.domain.model.Address


fun AddressResponse.toDomain(): Address {
    return Address(
        cep = cep,
        logradouro = logradouro,
        complemento = complemento,
        bairro = bairro,
        localidade = localidade,
        uf = uf,
        ddd = ddd
    )
}

fun Address.toEntity(): AddressEntity {
    return AddressEntity(
        id = id,
        cep = cep,
        logradouro = logradouro,
        bairro = bairro,
        localidade = localidade,
        uf = uf
    )
}

fun AddressEntity.toDomain(): Address {
    return Address(
        id = id,
        cep = cep,
        logradouro = logradouro,
        bairro = bairro,
        localidade = localidade,
        uf = uf,
        ddd = null,
        complemento = null
    )
}