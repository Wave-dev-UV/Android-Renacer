package com.example.gestrenacer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumberInput: EditText
    private lateinit var generateCodeButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        generateCodeButton = findViewById(R.id.generateCodeButton)


        generateCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                checkPhoneNumberInDatabase(phoneNumber)
            } else {
                Toast.makeText(this, "Introduce un número de teléfono", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkPhoneNumberInDatabase(phoneNumber: String) {
        db.collection("users")
            .whereEqualTo("celular", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    val document = documents.first()
                    val tieneAcceso = document.getBoolean("tieneAcceso") ?: false

                    if (tieneAcceso) {

                        sendVerificationCode(phoneNumber)
                    } else {

                        Toast.makeText(this, "El usuario no tiene acceso", Toast.LENGTH_LONG).show()
                    }
                } else {

                    Toast.makeText(this, "El número no está registrado", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar el número: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Envía el código de verificación al número de teléfono
    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+57$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Si la verificación se completa automáticamente
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity, "Error de verificación: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // Código enviado exitosamente, navega a la actividad de verificación
                    val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
                    intent.putExtra("storedVerificationId", verificationId)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Inicia sesión con las credenciales del código de verificación
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autenticación exitosa
                    Toast.makeText(this, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
