package com.example.gestrenacer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumberInput: EditText
    private lateinit var generateCodeButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        phoneNumberInput = view.findViewById(R.id.phoneNumberInput)
        generateCodeButton = view.findViewById(R.id.generateCodeButton)

        generateCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                checkPhoneNumberInDatabase(phoneNumber)
            } else {
                Toast.makeText(requireContext(), "Introduce un número de teléfono", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun checkPhoneNumberInDatabase(phoneNumber: String) {
        db.collection("users")
            .whereEqualTo("celular", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()

                    val rol = document.getString("rol") ?: "Feligrés"// Obtener el rol, con valor predeterminado "Feligres"

                    if (rol != "Feligrés") {
                        sendVerificationCode(phoneNumber, document.toObject(User::class.java).rol)
                    } else {
                        Toast.makeText(requireContext(), "El usuario no tiene acceso", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "El número no está registrado", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al verificar el número: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun sendVerificationCode(phoneNumber: String, rol: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+57$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(requireContext(), "Error de verificación: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // Aquí navegamos al VerifyFragment utilizando findNavController y pasando los datos mediante un Bundle
                    val bundle = Bundle().apply {
                        putString("storedVerificationId", verificationId)
                        putString("phoneNumber", phoneNumber)
                        putString("rol", rol)
                    }
                    findNavController().navigate(R.id.action_loginFragment_to_verifyFragment, bundle)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error en la autenticación", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
