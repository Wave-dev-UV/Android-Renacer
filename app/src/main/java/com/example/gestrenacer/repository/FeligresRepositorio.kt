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

            try {

                usersCollection.document(id).set(dataToSave).await()
                // Log para indicar que la actualización fue exitosa
                Log.d("FeligresRepositorio", "Documento actualizado con éxito: $id")
            } catch (e: Exception) {

                Log.e("FeligresRepositorio", "Error al actualizar el documento: ${e.message}")
            }
        } ?: Log.w("FeligresRepositorio", "Firestore ID es nulo")
    }
}
