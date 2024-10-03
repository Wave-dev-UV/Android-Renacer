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
        //Log.d("derp",snapshot.map { x -> x.toObject(User::class.java) }.toString())
        return snapshot.map { x -> x.toObject(User::class.java) }
    }

    fun saveUser(user: User){
        usersCollection.add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }
}