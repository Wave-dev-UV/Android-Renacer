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


    suspend fun updateUser(feligres: Feligres) {
        feligres.firestoreId?.let { id ->

            val dataToSave = hashMapOf<String, Any?>(
                "nombre" to feligres.nombre,
                "apellido" to feligres.apellido,
                "id" to feligres.id,
                "tipoId" to feligres.tipoId,
                "celular" to feligres.celular,
                "direccion" to feligres.direccion,
                "eps" to feligres.eps,
                "nombreContacto" to feligres.nombreContacto,
                "celularContacto" to feligres.celularContacto,
                "parentescoContacto" to feligres.parentescoContacto,
                "direccionContacto" to feligres.direccionContacto,
                "esLider" to feligres.esLider,
                "tieneAcceso" to feligres.tieneAcceso,
                "estadoAtencion" to feligres.estadoAtencion

            )


            usersCollection.document(id).set(dataToSave).await()
        }
    }

}