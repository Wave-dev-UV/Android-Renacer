package com.example.gestrenacer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_sms)


        auth = FirebaseAuth.getInstance()
        storedVerificationId = intent.getStringExtra("storedVerificationId") ?: ""

        val codeInput = findViewById<EditText>(R.id.codeInput)
        val verifyButton = findViewById<Button>(R.id.verifyButton)

        verifyButton.setOnClickListener {
            val code = codeInput.text.toString()
            if (code.isNotEmpty()) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Introduce el código de verificación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Autenticación exitosa", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "El código de verificación es incorrecto", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
