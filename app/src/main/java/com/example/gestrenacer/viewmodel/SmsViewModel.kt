package com.example.gestrenacer.viewmodel

import SmsWorker
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestrenacer.models.Sms
import com.example.gestrenacer.repository.SmsRepositorio
import com.example.gestrenacer.repository.UserRepositorio
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
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

    fun enviarSms(texto: String, contexto: Context, grupo: String = "", filtros: List<String> = listOf()) {
        viewModelScope.launch {
            _await.value = 2
            _progress.value = true
            try {
                val users = usuarios.value as List<String>

                val cant = users.size / 30
                val res = users.size % 30
                var inicio = 0

                for (x in 1..(cant + 1)) {
                    val list = (
                            if (x != cant + 1) {
                                users.subList(inicio, 30 * x).toTypedArray()
                            } else users.subList(inicio, inicio + res).toTypedArray())

                    val inputData = Data.Builder()
                        .putString("mensaje", texto)
                        .putStringArray("usuarios", list)
                        .build()

                    val notificationWork = OneTimeWorkRequestBuilder<SmsWorker>()
                        .setInputData(inputData)
                        .setInitialDelay(3 * (x - 1).toLong(), TimeUnit.MINUTES)
                        .build()

                    WorkManager.getInstance(contexto).enqueue(notificationWork)

                    inicio = 30 * x
                }
                _operacion.value = 1
            guardarSms(texto, grupo, filtros)

            }
            catch (e: Exception){
                _operacion.value = 2
            } finally {
                _await.value = 0
                _progress.value = false
            }
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
