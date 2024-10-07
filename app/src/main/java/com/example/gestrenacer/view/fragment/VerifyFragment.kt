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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class VerifyFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationCodeInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var storedVerificationId: String
    private lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verify_sms, container, false)

        auth = FirebaseAuth.getInstance()

        // Obtener argumentos pasados desde el LoginFragment
        storedVerificationId = arguments?.getString("storedVerificationId") ?: ""
        phoneNumber = arguments?.getString("phoneNumber") ?: ""

        verificationCodeInput = view.findViewById(R.id.verificationCodeInput)
        verifyButton = view.findViewById(R.id.verifyButton)

        verifyButton.setOnClickListener {
            val verificationCode = verificationCodeInput.text.toString().trim()
            if (verificationCode.isNotEmpty()) {
                verifyCode(verificationCode)
            } else {
                Toast.makeText(requireContext(), "Introduce el código de verificación", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Verificación exitosa", Toast.LENGTH_SHORT).show()


                    findNavController().navigate(R.id.action_verifyFragment_to_listarFragment)

                } else {
                    Toast.makeText(requireContext(), "Error en la verificación", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
