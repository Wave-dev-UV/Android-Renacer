package com.example.gestrenacer.viewmodel

import android.util.Log
import java.text.Normalizer
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
): ViewModel() {
    private val _listaUsers = MutableLiveData<MutableList<User>>()
    val listaUsers: LiveData<MutableList<User>> = _listaUsers

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    private val _rol = MutableLiveData("Feligrés")
    val rol: LiveData<String> = _rol

    private val _filtros = MutableLiveData<List<List<String>>>()
    val filtros: LiveData<List<List<String>>> = _filtros

    private val _orden = MutableLiveData<List<String>>()
    val orden: LiveData<List<String>> = _orden

    fun getFeligreses(fechaInicial: Timestamp, fechaFinal: Timestamp,
                      filtroEstcivil: List<String>,
                      filtroSexo: List<String>,
                      filtroLlamado: List<String>,
                      critOrden: String = "nombre", escalaOrden: String = "ascendente") {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val aInicio = fechaInicial.toDate().year.toString()
                val aFinal = fechaFinal.toDate().year.toString()
                val users = repository.getUsers(filtroSexo,filtroEstcivil,filtroLlamado,
                    fechaInicial,fechaFinal,critOrden,escalaOrden)

                _filtros.value = listOf(filtroSexo, filtroEstcivil,
                    listOf(aInicio,aFinal), filtroLlamado)
                _orden.value = listOf(critOrden, escalaOrden)
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

    fun eliminarUsuarios(users: MutableList<User>?) {
        viewModelScope.launch {
            try {
                // Eliminar usuarios del repositorio
                repository.eliminarUsuarios(users)
                Log.d("UserViewModel", "Usuarios eliminados con éxito")

                // Actualiza la lista de usuarios después de eliminar
                val updatedList = _listaUsers.value?.filterNot { user ->
                    users?.any { it.firestoreID == user.firestoreID } as Boolean
                }
                _listaUsers.value = updatedList?.toMutableList() // Actualiza la lista con los usuarios restantes

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al eliminar usuarios: ${e.message}")
            }
        }
    }

    fun colocarRol(rol: String?){
        _rol.value = rol
    }

    fun borrarUsuario(user: User){
        viewModelScope.launch {
            _progresState.value = true
            try {
                repository.borrarUsuario(user)
                _progresState.value = false
            } catch (e: Exception) {
                _progresState.value = false
            }
        }
    }
}