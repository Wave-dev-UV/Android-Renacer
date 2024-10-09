package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUsers(): List<User>{
        val snapshot = usersCollection.get().await()
        return snapshot.map { x ->
            val obj = x.toObject(User::class.java)
            obj.firestoreID = x.id
            obj
        }
    }

    suspend fun saveUser(user: User){
        usersCollection.add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    suspend fun updateUser(feligres: User) {
        feligres.firestoreID?.let { id ->
            try {

                usersCollection.document(id).set(feligres).await()


                Log.d("FeligresRepositorio", "Documento actualizado con éxito: $id")
            } catch (e: Exception) {
                Log.e("FeligresRepositorio", "Error al actualizar el documento: ${e.message}")
            }
        } ?: Log.w("FeligresRepositorio", "Firestore ID es nulo")
    }
}
