package com.example.gestrenacer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.repository.UserRepositorio
import com.example.gestrenacer.utils.FiltrosAux
import com.google.firebase.Timestamp
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

    private val _grupoActivado = MutableLiveData(false)
    val grupoActivado: LiveData<Boolean> = _grupoActivado

    private val _await = MutableLiveData(0)
    val await: LiveData<Int> = _await

    fun obtenerMiembrosGrupo(
        fechaInicial: Timestamp, fechaFinal: Timestamp,
        filtroEstcivil: List<String>,
        filtroSexo: List<String>,
        filtroLlamado: List<String>,
        critOrden: String = "nombre", escalaOrden: String = "ascendente"
    ) {
        viewModelScope.launch {
            try {
                val res = userRepositorio.getUsers(
                    filtroSexo, filtroEstcivil, filtroLlamado,
                    fechaInicial, fechaFinal, critOrden, escalaOrden
                )

                _usuarios.value = res.map { x -> x.celular }.toMutableList()
                _await.value = 1
                println("grupo resultante ${_usuarios.value}")
            } catch (e: Exception) {
                println("Errooor $e")
            }
        }
    }

    fun enviarSms(texto: String) {
        viewModelScope.launch {
            _await.value = 2
            _progress.value = true
            println("aki")
            val res = userRepositorio.enviarSms(texto, usuarios.value as List<String>)
            println("sms")
            when (res) {
                true -> _operacion.value = 1
                false -> _operacion.value = 2
            }
            _progress.value = false
        }
    }

    fun iniciarUsuarios(lista: MutableList<String>) {
        _usuarios.value = lista
    }

    fun cambiarGuardado(guardado: Boolean) {
        _guardado.value = guardado
    }

    fun cambiarGrupoActivado(activado: Boolean) {
        _grupoActivado.value = activado
    }

    fun cambiarOperacion(valor: Int) {
        _operacion.value = valor
    }

    fun cambiarAwait(valor: Int){
        _await.value = valor
    }
}
