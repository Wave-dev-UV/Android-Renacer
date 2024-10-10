package com.example.gestrenacer.viewmodel

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
): ViewModel() {
    private val _listaUsers = MutableLiveData<List<User>>()
    val listaUsers: LiveData<List<User>> = _listaUsers

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    fun getFeligreses(){
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listaUsers.value = repository.getUsers()
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
}