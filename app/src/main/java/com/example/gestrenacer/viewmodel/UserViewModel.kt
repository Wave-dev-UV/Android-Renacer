package com.example.gestrenacer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.UserRepositorio
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepositorio
) : ViewModel() {

    private val _listaUsers = MutableLiveData<List<User>>()
    val listaUsers: LiveData<List<User>> = _listaUsers

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    private val _rol = MutableLiveData("Feligrés")
    val rol: LiveData<String> = _rol

    var filtros: List<List<String>> = listOf()

    var orden: List<String> = listOf()

    fun getFeligreses(fechaInicial: Timestamp, fechaFinal: Timestamp,
                      filtroEstcivil: List<String>,
                      filtroSexo: List<String>,
                      critOrden: String = "nombre", escalaOrden: String = "ascendente"){
        viewModelScope.launch {
            _progresState.value = true
            try {
                val aInicio = fechaInicial.toDate().year.toString()
                val aFinal = fechaFinal.toDate().year.toString()
                val users = repository.getUsers(filtroSexo,filtroEstcivil,
                    fechaInicial,fechaFinal,critOrden,escalaOrden)

                filtros = listOf(filtroSexo, filtroEstcivil, listOf(aInicio,aFinal))
                orden = listOf(critOrden, escalaOrden)
                _listaUsers.value = users
                _progresState.value = false
            } finally {
                _progresState.value = false
            }
        }
    }

    fun crearUsuario(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun editarUsuario(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun colocarRol(rol: String?) {
        _rol.value = rol ?: "Feligrés"
    }
}