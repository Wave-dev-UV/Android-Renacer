package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.Plantilla
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlantillaRepositorio @Inject constructor() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val plantillasCollection = db.collection("plantilla")

    suspend fun getAllPlantillas(): List<Plantilla> {
        return withContext(Dispatchers.IO) {
            val plantillasList = mutableListOf<Plantilla>()
            try {
                // Usa 'plantillasCollection' en lugar de crear una nueva instancia
                val snapshot = plantillasCollection.get().await()
                Log.d("PlantillaRepository", "Documentos obtenidos: ${snapshot.documents.size}")

                for (document in snapshot.documents) {
                    val plantilla = document.toObject(Plantilla::class.java)
                    plantilla?.let { plantillasList.add(it) }
                }
            } catch (e: Exception) {
                Log.e("PlantillaRepository", "Error al obtener plantillas: ${e.message}")
            }
            plantillasList
        }
    }

    suspend fun savePlantilla(plantilla: Plantilla) {
        withContext(Dispatchers.IO) {
            try {
                // Usa 'plantillasCollection' para guardar la plantilla
                plantillasCollection.document(plantilla.id).set(plantilla).await()
                Log.d("PlantillaRepository", "Plantilla guardada: ${plantilla.id}")
            } catch (e: Exception) {
                Log.e("PlantillaRepository", "Error al guardar plantilla: ${e.message}")
            }
        }
    }

//    suspend fun eliminarPlantilla(plantillaId: String) {
//        withContext(Dispatchers.IO) {
//            try {
//                plantillasCollection.document(plantillaId).delete().await()
//                Log.d("PlantillaRepository", "Plantilla eliminada: $plantillaId")
//            } catch (e: Exception) {
//                Log.e("PlantillaRepository", "Error al eliminar plantilla: ${e.message}")
//            }
//        }
//    }
    suspend fun eliminarPlantillas(plantillas: List<Plantilla>) {
        withContext(Dispatchers.IO) {
            try {
                for (plantilla in plantillas) {
                    plantillasCollection.document(plantilla.id).delete().await()
                    Log.d("PlantillaRepository", "Plantilla eliminada: ${plantilla.id}")
                }
            } catch (e: Exception) {
                Log.e("PlantillaRepository", "Error al eliminar plantillas: ${e.message}")
            }
        }
    }

}
