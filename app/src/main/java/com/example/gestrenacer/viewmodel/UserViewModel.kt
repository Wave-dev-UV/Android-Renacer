package com.example.gestrenacer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.UserRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepositorio
): ViewModel() {
    private val _listaUsers = MutableLiveData<List<User>>()
    val listaUsers: LiveData<List<User>> = _listaUsers

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    private val _rol = MutableLiveData("Feligrés")
    val rol: LiveData<String> = _rol

    fun getFeligreses(filtros: List<List<String>>, fechaInicial: Date, fechaFinal: Date){
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listaUsers.value = repository.getUsers(filtros,fechaInicial,fechaFinal)
                _progresState.value = false
            } catch (e: Exception) {
                _progresState.value = false
            }
        }
    }

    fun crearUsuario(user: User){
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun editarUsuario(user: User){
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun colocarRol(rol: String?){
        _rol.value = rol
    }

    fun cerrarSesion(){
        repository.cerrarSesion()
    }
}