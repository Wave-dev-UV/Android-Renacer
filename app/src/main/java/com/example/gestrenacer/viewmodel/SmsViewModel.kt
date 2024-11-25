package com.example.gestrenacer.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.content.ContextCompat.startActivity
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
            val res = smsRepositorio.enviarSms(usuarios.value as List<String>,texto)
            when (res) {
                true -> {
                    _operacion.value = 1
                    guardarSms(texto, grupo, filtros)
                }
                false -> {
                    _operacion.value = 2
                }
            }
            _await.value = 0
            _progress.value = false
        }
    }

    private fun guardarSms(texto: String, grupo: String, filtros: List<String>) {
        val fecha = Timestamp.now()
        val envio = "${usuarios.value?.size} personas."
        var grupoEnvio = ""
        var filtro = filtros

        if (grupoActivado.value as Boolean) {
            grupoEnvio = grupo
            filtro = listOf()
        }

        viewModelScope.launch {
            smsRepositorio.guardarSms(Sms(filtro, texto, envio, fecha, grupoEnvio))
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
