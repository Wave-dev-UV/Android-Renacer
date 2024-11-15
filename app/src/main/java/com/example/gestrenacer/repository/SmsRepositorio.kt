package com.example.gestrenacer.repository

import com.example.gestrenacer.models.PeticionEnviarSms
import com.example.gestrenacer.models.Sms
import com.example.gestrenacer.webservices.SmsService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsRepositorio @Inject constructor(private val smsService: SmsService) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val smsCollection = db.collection("sms")

    suspend fun guardarSms(sms: Sms) {
        withContext(Dispatchers.IO) {
            try {
                smsCollection.add(sms).await()
            } catch (e: Exception) {
                println("Error guardar sms $e")
            }
        }
    }

    suspend fun verSms(): List<Sms> {
        return withContext(Dispatchers.IO) {
            try {
                val lista = smsCollection.orderBy("fecha", Query.Direction.DESCENDING).get().await()

                lista.map { it.toObject(Sms::class.java) }
            } catch (e: Exception) {
                println("Error al ver sms $e")
                listOf()
            }
        }
    }

    suspend fun enviarSms(mensaje: String, numeros: List<String>): Boolean {
        val token = auth.getAccessToken(true).await().token as String
        val telefono = auth.currentUser?.phoneNumber as String
        val body = PeticionEnviarSms(telefono.split("+57")[1], mensaje, numeros)

        return withContext(Dispatchers.IO) {
            try {
                val response = smsService.enviarSms(body, token)
                response.resultado
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}