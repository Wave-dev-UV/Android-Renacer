package com.example.gestrenacer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.databinding.FragmentLoginBinding
import com.example.gestrenacer.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (authViewModel.isUserVerified() && !authViewModel.isReVerificationNeeded()) {
            showBiometricPrompt()
        }

        binding.generateCodeButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                authViewModel.checkUserAccess(phoneNumber, requireActivity())
            } else {
                Toast.makeText(requireContext(), "Introduce un número de teléfono", Toast.LENGTH_SHORT).show()
            }
        }


        authViewModel.accessGranted.observe(viewLifecycleOwner, Observer { hasAccess ->
            if (hasAccess == false) {
                Toast.makeText(requireContext(), "El usuario no tiene acceso o no está registrado", Toast.LENGTH_LONG).show()
            }
        })

        authViewModel.verificationId.observe(viewLifecycleOwner, Observer { verificationId ->
            val bundle = Bundle().apply {
                putString("verificationId", verificationId)
                putString("phoneNumber", binding.phoneNumberInput.text.toString())
                putString("rol", authViewModel.rol.value)
            }
            Log.d("prueba11","rol al verify: ${bundle.get("rol")}")
            findNavController().navigate(R.id.action_loginFragment_to_verifyFragment, bundle)
        })


        authViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })

        authViewModel.progress.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(requireContext(), "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val bundle = Bundle().apply {
                    putString("rol", authViewModel.getUserRole())
                }
                Log.d("Prueba33","rol: ${authViewModel.getUserRole()}")
                findNavController().navigate(R.id.action_loginFragment_to_listarFragment, bundle)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Utiliza tu huella digital para acceder")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
