package com.example.gestrenacer.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.gestrenacer.models.PeticionDesuscribir
import com.example.gestrenacer.models.PeticionEnviarSms
import com.example.gestrenacer.models.PeticionSuscribir
import com.example.gestrenacer.models.SmsSubsRes
import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.Constants.ARN_SMS
import com.example.gestrenacer.webservices.SmsService
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

class UserRepositorio @Inject constructor(
    private val smsService: SmsService) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    suspend fun getUsers(
        filtroSexo: List<String>, filtroEstCivil: List<String>,
        filtroLlamado: List<String>, fechaInicial: Timestamp,
        fechaFinal: Timestamp, critOrden: String,
        escalaOrden: String
    ): MutableList<User> {
        val order = (
                if (escalaOrden == "ascendente") Query.Direction.ASCENDING
                else Query.Direction.DESCENDING
                )

        val snapshot =
            usersCollection.whereIn("sexo", filtroSexo).whereIn("estadoCivil", filtroEstCivil)
                .whereIn("estadoAtencion", filtroLlamado)
                .whereGreaterThan("fechaNacimiento", fechaInicial)
                .whereLessThan("fechaNacimiento", fechaFinal).orderBy(critOrden, order).get()
                .await()

        return snapshot.map { x ->
            val obj = x.toObject(User::class.java)
            obj.firestoreID = x.id
            obj
        }.toMutableList()
    }

    suspend fun saveUser(user: User): Int {
        try {
            var res = 0
            val newUser = user.copy(fechaCreacion = Timestamp.now())
            /*val resSubs = suscribirSms(user.celular)

            if (resSubs.resultado){
                newUser.arn = resSubs.arn
                usersCollection.add(newUser).await()
            }
            else{
                throw Exception()
            }*/
            val numRepetido = usersCollection.whereEqualTo("celular", user.celular).get().await()

            if (numRepetido.isEmpty) usersCollection.add(newUser).await()
            else res = 1

            return res
        } catch (e: Exception) {
            return 2
        }
    }

    suspend fun updateUser(feligres: User, prevNum: String, llamado: Boolean): Int {
        return feligres.firestoreID.let { id ->
            try {
                var res = 0
                /*var arn = SmsSubsRes(false, "")

                if (prevNum.isNotEmpty()) {
                    val resDesuscribir = desuscribirSms(prevNum)

                    if (resDesuscribir) arn = suscribirSms(feligres.celular)
                    else throw Exception("No se ha podido desuscribir al usuario.")

                    if (arn.resultado) feligres.arn = arn.arn
                    else throw Exception("No se ha podido suscribir al usuario")
                }*/

                val numRepetido = usersCollection.whereEqualTo("celular", feligres.celular).get().await()

                if (numRepetido.isEmpty || llamado) usersCollection.document(id).set(feligres).await()
                else res = 1

                res
            } catch (e: Exception) {
                2
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
                val rol = document.getString("rol")
                    ?: "Feligrés"  // Rol predeterminado si no se encuentra
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
    suspend fun eliminarUsuarios(users: MutableList<User>?): Boolean {
        return withContext(Dispatchers.IO) {
            val list = users as MutableList<User>
            try {
                for (user in list) {
                    /*val res = desuscribirSms(user.arn)
                    if (res) usersCollection.document(user.firestoreID).delete().await()
                    else throw Exception("No se podido desuscribir al usuario)*/
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
                /*val res = desuscribirSms(user.arn)
                if (res) usersCollection.document(user.firestoreID).delete().await()
                else throw  Exception()*/
                usersCollection.document(user.firestoreID).delete().await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun suscribirSms(numero: String): SmsSubsRes {
        val token = auth.getAccessToken(true).await().token as String
        val telefono = auth.currentUser?.phoneNumber as String
        val body = PeticionSuscribir(telefono, ARN_SMS, numero)

        return withContext(Dispatchers.IO) {
            try {
                val response = smsService.suscribirSms(body, token)
                response
            } catch (e: Exception) {
                e.printStackTrace()
                SmsSubsRes(false, "")
            }
        }
    }

    suspend fun desuscribirSms(arn: String): Boolean {
        val token = auth.getAccessToken(true).await().token as String
        val telefono = auth.currentUser?.phoneNumber as String
        val body = PeticionDesuscribir(arn, telefono)

        return withContext(Dispatchers.IO) {
            try {
                val response = smsService.desuscribirSms(body, token)
                response.resultado
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun enviarSms(mensaje: String, numeros: List<String>): Boolean {
        val token = auth.getAccessToken(true).await().token as String
        val telefono = auth.currentUser?.phoneNumber as String
        val body = PeticionEnviarSms(telefono, mensaje, numeros)

        return withContext(Dispatchers.IO) {
            try {
                val response = smsService.enviarSms(body, token)
                response.resultado
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
