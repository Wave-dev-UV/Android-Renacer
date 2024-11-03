package com.example.gestrenacer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.repository.UserRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio
) : ViewModel() {

    private val _usuarios = MutableLiveData<MutableList<String>>()
    val usuarios: LiveData<MutableList<String>> = _usuarios

    private val _progress = MutableLiveData(false)
    val progress: LiveData<Boolean> = _progress

    private val _operacion = MutableLiveData(0)
    val operacion: LiveData<Int> = _operacion

    private val _guardado = MutableLiveData(false)
    val guardado: LiveData<Boolean> = _guardado

    private val _grupoActivado= MutableLiveData(false)
    val grupoActivado: LiveData<Boolean> = _grupoActivado

    fun enviarSms(texto: String, grupos: Boolean = false) {
        viewModelScope.launch {
            _progress.value = true
            if (!grupos) {
                val res = userRepositorio.enviarSms(texto, usuarios.value as List<String>)
                when (res) {
                    true -> _operacion.value = 1
                    false -> _operacion.value = 2
                }
            }
            _progress.value = false
        }
    }

    fun iniciarUsuarios(lista: MutableList<String>){
        _usuarios.value = lista
    }

    fun cambiarGuardado(guardado: Boolean){
        _guardado.value = guardado
    }

    fun verGuardado(): Boolean {
        return guardado.value as Boolean
    }

    fun verGrupoActivado():Boolean {
        return grupoActivado.value as Boolean
    }
}
