package com.example.gestrenacer.repository

import com.example.gestrenacer.models.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val groupsCollection = db.collection("groups")

//    suspend fun getGroups (): {
//
//    }

    suspend fun saveGroup(group: Group) {
        withContext(Dispatchers.IO) {
            groupsCollection.add(group).await()
        }
//        try {
//            groupsCollection.add(group).await()
//        } catch (e: Exception) {
//            Log.e("GroupRepositorio", "Error al agregar el grupo: ${e.message}")
//        }
    }
}