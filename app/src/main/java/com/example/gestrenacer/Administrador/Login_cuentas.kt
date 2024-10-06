package com.example.gestrenacer.Administrador

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestrenacer.R
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions



class Login_cuentas : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth
    private var verificationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_cuentas)
        firebaseAuth = FirebaseAuth.getInstance()


    }

    private fun verifyCode(code: String) {
        if (verificationId != null) { // Verifica que el ID no sea nulo
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Log.w("LoginActivity", "verificationId no está disponible")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // El inicio de sesión fue exitoso
                    Log.d("LoginActivity", "Inicio de sesión exitoso")
                    // Aquí puedes redirigir al usuario a la pantalla principal de tu aplicación
                } else {
                    // El inicio de sesión falló
                    Log.w("LoginActivity", "Inicio de sesión fallido", task.exception)
                }
            }
    }

    }
}