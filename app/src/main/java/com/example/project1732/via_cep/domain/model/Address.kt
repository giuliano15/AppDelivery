package com.example.project1732.via_cep.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: Long = 0,
    val cep: String?,
    val logradouro: String?,
    val complemento: String?,
    val bairro: String?,
    val localidade: String?,
    val uf: String?,
    val ddd: String?,
): Parcelable {

    fun getFullAddress(): String {
        return "$logradouro - $bairro - $localidade - $uf  $cep"
    }
}

