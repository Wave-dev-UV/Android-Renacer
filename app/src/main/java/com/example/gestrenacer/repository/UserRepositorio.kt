package com.example.gestrenacer.repository

import android.app.Activity
import android.util.Log
import com.example.gestrenacer.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepositorio @Inject constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")


    suspend fun getUsers(filtroSexo: List<String>,filtroEstCivil: List<String>,
                         filtroLlamado: List<String>,fechaInicial:Timestamp,
                         fechaFinal:Timestamp, critOrden: String,
                         escalaOrden: String): List<User> {
        val order = (
            if (escalaOrden == "ascendente") Query.Direction.ASCENDING
            else Query.Direction.DESCENDING
        )

        val snapshot = usersCollection.whereIn("sexo",filtroSexo).
            whereIn("estadoCivil",filtroEstCivil).
            whereIn("estadoAtencion",filtroLlamado).
            whereGreaterThan("fechaNacimiento", fechaInicial).
            whereLessThan("fechaNacimiento", fechaFinal).
            orderBy(critOrden, order).get().await()

        return snapshot.map { x ->
            val obj = x.toObject(User::class.java)
            obj.firestoreID = x.id
            obj
        }
    }

    suspend fun saveUser(user: User) {
        try {
            val newUser = user.copy(fechaCreacion = Timestamp.now())
            usersCollection.add(newUser).await()
            Log.d("UserRepositorio", "Usuario agregado con éxito")
        } catch (e: Exception) {
            Log.e("UserRepositorio", "Error al agregar el usuario: ${e.message}")
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


    suspend fun getUserByPhone(phoneNumber: String): String? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("celular", phoneNumber)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val rol = document.getString("rol") ?: "Feligrés"  // Rol predeterminado si no se encuentra
                if (rol != "Feligrés") {
                    rol
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepositorio", "Error al obtener usuario: ${e.message}")
            null
        }
    }

    suspend fun getPendingUsers(): List<User> {
        val snapshot = usersCollection
            .whereEqualTo("estadoAtencion", "Por Llamar")
            .get().await()
        return snapshot.map { x ->
            val obj = x.toObject(User::class.java)
            obj.firestoreID = x.id
            obj
        }
    }

    fun sendVerificationCode(
        phoneNumber: String,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        activity: Activity
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean {
        return try {
            auth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepositorio", "Error en la autenticación: ${e.message}")
            false
        }
    }

    // Método para eliminar uno o varios usuarios
    suspend fun eliminarUsuarios(users: List<User>) {
        withContext(Dispatchers.IO) {
            try {
                for (user in users) {
                    usersCollection.document(user.firestoreID).delete().await()
                }
                Log.d("UserRepositorio", "Usuarios eliminados con éxito: ${users.size}")
            } catch (e: Exception) {
                Log.e("UserRepositorio", "Error al eliminar usuarios: ${e.message}")
            }
        }
    }


    suspend fun borrarUsuario(user: User){
        withContext(Dispatchers.IO){
            try {
                usersCollection.document(user.firestoreID).delete().await()
            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        }
    }


}
