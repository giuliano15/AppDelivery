package com.example.project1732.via_cep.presenter.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.project1732.Helper.UserManager
import com.example.project1732.via_cep.data.mapper.toDomain
import com.example.project1732.via_cep.domain.local.usecase.DeleteAddressUseCase
import com.example.project1732.via_cep.domain.local.usecase.GetAllAddressUseCase
import com.example.project1732.via_cep.domain.local.usecase.InsertAddressUseCase
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ListAddressViewModel @Inject constructor(
    application: Application, // Injetando Application para obter o contexto
    private val getAllAddressUseCase: GetAllAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val insertAddressUseCase: InsertAddressUseCase,// Novo UseCase de deletar
) : AndroidViewModel(application) {

    private val _addressList = MutableLiveData(mutableListOf<Address>())
    val addressList: LiveData<MutableList<Address>> = _addressList

    private val _addressChanged = MutableLiveData(Unit)
    val addressChanged: LiveData<Unit> = _addressChanged

    // Instanciando o UserManager com o contexto da aplicação
    private val userManager: UserManager = UserManager.getInstance(application.applicationContext)

    init {
        getAllAddress()
    }


    fun getAllAddress() = viewModelScope.launch {
        getAllAddressUseCase.invoke().collect { addresses ->
            _addressList.value = addresses.map { it.toDomain() }.toMutableList()
        }
    }

    fun addressChanged() {
        _addressChanged.value = Unit
    }

    // Nova função para pegar o nome do usuário logado
    fun getUserName(): String {
        return userManager.getUserName() // Retorna o nome do usuário logado
    }

    // Função que pode ser chamada para verificar se o usuário está logado
    fun isUserLoggedIn(): Boolean {
        return userManager.isUserLoggedIn()
    }


    // Nova função para deletar endereço
    fun deleteAddress(address: Address) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            deleteAddressUseCase(address) // Chama o use case para deletar

            emit(StateView.Success("Endereço deletado com sucesso"))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(StateView.Error(message = e.message))
        }
    }
}
















