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
    private val _listaUsers = MutableLiveData<MutableList<User>>()
    val listaUsers: LiveData<MutableList<User>> = _listaUsers

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    private val _filtros = MutableLiveData<List<List<String>>>()
    val filtros: LiveData<List<List<String>>> = _filtros

    private val _orden = MutableLiveData<List<String>>()
    val orden: LiveData<List<String>> = _orden

    private val _resOperacion = MutableLiveData<Int>()
    val resOperacion: LiveData<Int> = _resOperacion

    fun getFeligreses(
        fechaInicial: Timestamp, fechaFinal: Timestamp,
        filtroEstcivil: List<String>,
        filtroSexo: List<String>,
        filtroLlamado: List<String>,
        critOrden: String = "nombre", escalaOrden: String = "ascendente"
    ) {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val aInicio = fechaInicial.toDate().year.toString()
                Log.d("1Aca","aca 1")
                val aFinal = fechaFinal.toDate().year.toString()
                Log.d("2Aca","aca 2")
                val users = repository.getUsers(
                    filtroSexo, filtroEstcivil, filtroLlamado,
                    fechaInicial, fechaFinal, critOrden, escalaOrden
                )
                Log.d("3Aca","aca 3")

                _filtros.value = listOf(
                    filtroSexo, filtroEstcivil,
                    listOf(aInicio, aFinal), filtroLlamado
                )
                _orden.value = listOf(critOrden, escalaOrden)
                _listaUsers.value = users
                _progresState.value = false
            }
            finally {
                _progresState.value = false
            }
        }
    }

    fun crearUsuario(user: User) {
        viewModelScope.launch {
            _progresState.value = true
            _resOperacion.value = repository.saveUser(user)
            _progresState.value = false
        }
    }

    fun editarUsuario(user: User, prevNum: String = "", llamado: Boolean = false) {
        viewModelScope.launch {
            val numAnt = (
                    if (prevNum.isNotEmpty() && (user.celular != prevNum)) prevNum
                    else ""
                )
            if (!llamado) _progresState.value = true
            _resOperacion.value = repository.updateUser(user, numAnt, llamado)
            if (!llamado) _progresState.value = false
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
                _listaUsers.value =
                    updatedList?.toMutableList() // Actualiza la lista con los usuarios restantes

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al eliminar usuarios: ${e.message}")
            }
        }
    }

    fun borrarUsuario(user: User) {
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