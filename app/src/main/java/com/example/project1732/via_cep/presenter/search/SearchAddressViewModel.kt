package com.example.project1732.via_cep.presenter.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.data.mapper.toEntity
import com.example.project1732.via_cep.domain.local.usecase.InsertAddressUseCase
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.domain.usecase.GetAddressUseCase
import com.example.project1732.via_cep.domain.usecase.UpdateAddressUseCase
import com.example.project1732.via_cep.util.StateView


import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SearchAddressViewModel @Inject constructor(
    private val getAddressUseCase: GetAddressUseCase,
    private val insertAddressUseCase: InsertAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase

) : ViewModel() {

    fun getAddress(cep: String) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            val address = getAddressUseCase(cep)

            emit(StateView.Success(address))
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(StateView.Error(message = e.message))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(StateView.Error(message = e.message))
        }
    }

    fun insertAddress(address: Address) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            val id = insertAddressUseCase(address)

            emit(StateView.Success(id))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(StateView.Error(message = e.message))
        }
    }

//    fun updateAddress(address: Address) = liveData(Dispatchers.IO) {
//        try {
//            emit(StateView.Loading())
//
//            updateAddressUseCase(address.toEntity()) // Converta Address para AddressEntity
//
//            emit(StateView.Success(Unit))
//        } catch (e: Exception) {
//            when (e) {
//                is HttpException -> {  // Tratar HttpException separadamente (ex: erro de rede)
//                    val errorMessage = "Erro ao atualizar endereço: ${e.message()}"
//                    emit(StateView.Error(errorMessage))
//                }
//
//                else -> {  // Tratar outros tipos de exceções genericamente
//                    val errorMessage = "Erro desconhecido ao atualizar endereço"
//                    emit(StateView.Error(errorMessage))
//                }
//            }
//        }
//    }


    fun updateAddress(addressEntity: AddressEntity) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            updateAddressUseCase(addressEntity) // Chama o caso de uso para atualizar

            emit(StateView.Success("Endereço atualizado com sucesso!"))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(StateView.Error(message = e.message))
        }
    }

}

//}