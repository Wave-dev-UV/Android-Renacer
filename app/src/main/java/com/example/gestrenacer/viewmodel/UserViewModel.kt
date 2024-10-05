package com.example.gestrenacer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Feligres
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.UserRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio
): ViewModel(){
    private val _listaFeligreses = MutableLiveData<List<Feligres>>()
    val listaFeligreses: LiveData<List<Feligres>> = _listaFeligreses

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    fun getFeligreses(){
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listaFeligreses.value = userRepositorio.getFeligreses()
                _progresState.value = false
                Log.d("here","aca")
            } catch (e: Exception) {
                Log.d("Error carga feligreses", e.toString())
                _progresState.value = false
            }
        }
    }

    fun crearUsuario(user: User){
        viewModelScope.launch {
            userRepositorio.saveUser(user)
        }
    }

    fun editarUsuario(user: User){
        viewModelScope.launch {
            userRepositorio.updateUser(user)
        }
    }
}