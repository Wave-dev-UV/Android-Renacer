package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.Feligres
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeligresRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun updateUser(feligres: Feligres) {
        feligres.firestoreId?.let { id ->
            try {

                usersCollection.document(id).set(feligres).await()


                Log.d("FeligresRepositorio", "Documento actualizado con éxito: $id")
            } catch (e: Exception) {
                Log.e("FeligresRepositorio", "Error al actualizar el documento: ${e.message}")
            }
        } ?: Log.w("FeligresRepositorio", "Firestore ID es nulo")
    }
}
