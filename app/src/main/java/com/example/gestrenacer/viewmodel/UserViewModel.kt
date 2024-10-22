package com.example.gestrenacer.viewmodel

import android.util.Log
import java.text.Normalizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.UserRepositorio
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

    fun getFeligreses() {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val users = repository.getUsers()
                _listaUsers.value = users.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() }))
            } catch (e: Exception) {

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
    
    // Eliminar un usuario
    fun eliminarUsuarios(users: List<User>) {
        viewModelScope.launch {
            _progresState.value = true
            try {
                repository.eliminarUsuarios(users)
                Log.d("UserViewModel", "Usuario eliminado con éxito")
                getFeligreses()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al eliminar el usuario: ${e.message}")
            } finally {
                _progresState.value = false
            }
        }
    }

    fun colocarRol(rol: String?) {
        _rol.value = rol ?: "Feligrés"
    }
}
