package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepositorio @Inject constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")


    suspend fun getUsers(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.map { x ->
            val obj = x.toObject(User::class.java)
            obj.firestoreID = x.id
            obj
        }
    }

    suspend fun saveUser(user: User) {
        try {
            usersCollection.add(user).await()
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


    fun sendVerificationCode(
        phoneNumber: String,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
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
}
