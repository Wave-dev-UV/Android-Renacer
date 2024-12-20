package com.example.gestrenacer.repository

import android.util.Log
import com.example.gestrenacer.models.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val groupsCollection = db.collection("groups")

    suspend fun getGroups(): List<Group> {
        return withContext(Dispatchers.IO) {
            val groupsList = mutableListOf<Group>()
            try {
                val snapshot = groupsCollection.get().await()
                snapshot.forEach { x ->
                    groupsList.add(x.toObject(Group::class.java))
                }
            } catch (e: Exception) {
                Log.e("GroupRepositorio", "Error retrieving groups data: ${e.message}", e)
            }
            groupsList
        }
    }

    suspend fun saveGroup(group: Group) {
        try {
            // Use the group name as the document ID
            groupsCollection.document(group.nombre).set(group).await()
        } catch (e: Exception) {
            Log.e("GroupRepositorio", "Error adding group: ${e.message}", e)
        }
    }

    suspend fun deleteGroup(group: Group) {
        try {
            groupsCollection.document(group.nombre).delete().await()
        } catch (e: Exception) {
            Log.e("GroupRepositorio", "Error deleting group: ${e.message}", e)
        }
    }
}
