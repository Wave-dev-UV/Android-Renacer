package com.example.gestrenacer.repository

import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.FiltrosAux
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositorio @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    suspend fun getUsers(
        filtroSexo: List<String>, filtroEstCivil: List<String>,
        filtroLlamado: List<String>, fechaInicial: Timestamp,
        fechaFinal: Timestamp, critOrden: String,
        escalaOrden: String
    ): MutableList<User> {
        var snapshot =
            usersCollection.whereIn("sexo", filtroSexo).whereIn("estadoCivil", filtroEstCivil)
                .whereIn("estadoAtencion", filtroLlamado)
                .whereGreaterThan("fechaNacimiento", fechaInicial)
                .whereLessThan("fechaNacimiento", fechaFinal).get()
                .await().map { x ->
                    val obj = x.toObject(User::class.java)
                    obj.firestoreID = x.id
                    obj
                }.toMutableList()

        if (filtroSexo.size == 2 && filtroEstCivil.size == 5) {
            val vacioSexo = verCampo("sexo", "", filtroLlamado, fechaInicial, fechaFinal)

            val vacioEst = verCampo("estadoCivil", "", filtroLlamado, fechaInicial, fechaFinal)

            snapshot.addAll(vacioSexo)
            snapshot.addAll(vacioEst)
        }

        snapshot = FiltrosAux.ordenar(snapshot, critOrden, escalaOrden)

        return snapshot
    }

    suspend fun verCampo(
        campo: String, valor: String, filtroLlamado: List<String>, fechaInicial: Timestamp,
        fechaFinal: Timestamp
    ): MutableList<User> {
        val snapshot = usersCollection.whereEqualTo(campo, valor)
            .whereIn("estadoAtencion", filtroLlamado)
            .whereGreaterThan("fechaNacimiento", fechaInicial)
            .whereLessThan("fechaNacimiento", fechaFinal).get()
            .await().map { x ->
                val obj = x.toObject(User::class.java)
                obj.firestoreID = x.id
                obj
            }.toMutableList()

        return snapshot
    }

    suspend fun saveUser(user: User): Int {
        try {
            var res = 0
            val newUser = user.copy(fechaCreacion = Timestamp.now())
            val numRepetido = usersCollection.whereEqualTo("correo", user.correo).get().await()

            if (numRepetido.isEmpty) usersCollection.add(newUser).await()
            else res = 1

            return res
        } catch (e: Exception) {
            return 2
        }
    }

    suspend fun updateUser(feligres: User, prevNumber: String = "", llamado: Boolean): Int {
        return feligres.firestoreID.let { id ->
            try {
                var res = 0
                var vacio = prevNumber.isEmpty()

                if (prevNumber.isNotEmpty()) {
                    vacio = usersCollection.whereEqualTo("correo", feligres.correo).get()
                        .await().isEmpty
                }
                if (vacio || llamado) {
                    usersCollection.document(id).set(feligres).await()
                } else res = 1

                res
            } catch (e: Exception) {
                2
            }
        }
    }


    suspend fun  verUsuarioPorCorreo(correo: String): String {
        return try {
            val snapshot = usersCollection.whereEqualTo("correo",correo).get().await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                document.getString("rol") ?: "Feligrés"
            }
            else {
                cerrarSesion()
                "Feligrés"
            }

        } catch (e: Exception) {
            "Feligrés"
        }
    }

    // Método para eliminar uno o varios usuarios
    suspend fun eliminarUsuarios(users: MutableList<User>?): Boolean {
        return withContext(Dispatchers.IO) {
            val list = users as MutableList<User>
            try {
                for (user in list) {
                    usersCollection.document(user.firestoreID).delete().await()
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }


    suspend fun borrarUsuario(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(user.firestoreID).delete().await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun cerrarSesion() {
        Firebase.auth.signOut()
    }
}
