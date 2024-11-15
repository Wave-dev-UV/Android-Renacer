package com.example.gestrenacer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Sms
import com.example.gestrenacer.repository.SmsRepositorio
import com.example.gestrenacer.repository.UserRepositorio
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio,
    private val smsRepositorio: SmsRepositorio
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

    private val _listaSms = MutableLiveData<List<Sms>>()
    val listaSms: LiveData<List<Sms>> = _listaSms

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
            } catch (e: Exception) {
                println("Errooor $e")
            }
        }
    }

    fun enviarSms(texto: String, grupo: String = "", filtros: List<String> = listOf()) {
        viewModelScope.launch {
            _await.value = 2
            _progress.value = true
            val res = smsRepositorio.enviarSms(texto, usuarios.value as List<String>)
            when (res) {
                true -> {
                    _operacion.value = 1
                    guardarSms(texto, grupo, filtros)
                }
                false -> {
                    _operacion.value = 2
                }
            }
            _progress.value = false
        }
    }

    private fun guardarSms(texto: String, grupo: String, filtros: List<String>) {
        val fecha = Timestamp.now()
        var envio = "A ${usuarios.value?.size} personas."

        if (grupoActivado.value as Boolean) {
            envio = "Enviado al grupo ${grupo}."
        }

        viewModelScope.launch {
            smsRepositorio.guardarSms(Sms(filtros, texto, envio, fecha))
        }
    }

    fun verSms(){
        viewModelScope.launch{
            try{
                _progress.value = true
                _listaSms.value = smsRepositorio.verSms()
                _progress.value = false
            } catch (e: Exception){
                _progress.value = false
            }
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
