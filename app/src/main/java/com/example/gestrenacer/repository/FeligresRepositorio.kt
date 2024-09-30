package com.example.gestrenacer.repository

import com.example.gestrenacer.models.Feligres
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeligresRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getFeligreses(): List<Feligres>{
        //Pendiente definir el nombre de la tabla
        val snapshot = db.collection("feligreses").get().await()
        return snapshot.map { x -> x.toObject(Feligres::class.java) }
    }
}