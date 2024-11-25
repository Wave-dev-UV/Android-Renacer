package com.example.gestrenacer.repository

import com.example.gestrenacer.models.Sms
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsRepositorio @Inject constructor() {
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
}