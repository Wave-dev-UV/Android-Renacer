package com.example.gestrenacer.viewmodel

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

    private val _filtroUsuarios = MutableLiveData<List<User>>()
    val filtroUsuarios: LiveData<List<User>> = _filtroUsuarios

    private val _rol = MutableLiveData("Feligrés")
    val rol: LiveData<String> = _rol


    fun getFeligreses() {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val users = repository.getUsers()
                _listaUsers.value = users.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() }))
                _filtroUsuarios.value = _listaUsers.value
            } catch (e: Exception) {
                // Manejo de errores
            } finally {
                _progresState.value = false
            }
        }
    }

    fun filtrarUsuarios(query: String) {
        val normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")

        _listaUsers.value?.let { lista ->
            val filteredList = lista.filter { user ->
                val nombre = Normalizer.normalize(user.nombre, Normalizer.Form.NFD)
                    .replace("[^\\p{ASCII}]".toRegex(), "")
                val apellido = Normalizer.normalize(user.apellido, Normalizer.Form.NFD)
                    .replace("[^\\p{ASCII}]".toRegex(), "")
                nombre.contains(normalizedQuery, ignoreCase = true) ||
                        apellido.contains(normalizedQuery, ignoreCase = true)
            }
            _filtroUsuarios.value = filteredList.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() }))
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
