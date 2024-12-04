package com.example.gestrenacer.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.repository.UserRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio
) : ViewModel() {

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = _progress

    private val _rol = MutableLiveData("")
    val rol: LiveData<String> = _rol

    fun cerrarSesion() {
        userRepositorio.cerrarSesion()
    }

    fun verificarAcceso(correo: String) {
        viewModelScope.launch {
            _progress.value = true
            val res = userRepositorio.verUsuarioPorCorreo(correo)

            _rol.value = res
            _progress.value = false
        }
    }
}
