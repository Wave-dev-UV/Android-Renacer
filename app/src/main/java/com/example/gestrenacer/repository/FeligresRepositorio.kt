package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.Feligres
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeligresRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getFeligreses(): List<Feligres>{
        //Pendiente definir el nombre de la tabla
        val snapshot = db.collection("feligreses").get().await()
        return snapshot.map { x -> x.toObject(Feligres::class.java) }
    }

    fun saveUser(feligres: Feligres){
        usersCollection.add(feligres)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }
}