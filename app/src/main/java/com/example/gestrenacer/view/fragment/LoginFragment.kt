package com.example.gestrenacer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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

    private var bloqueado = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnUsarHuella = binding.lblUsarHuella
        val preferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val verificado = preferences.getBoolean("user_verified", false)

        if (verificado && !isReVerificationNeeded()) {
            btnUsarHuella.visibility = View.VISIBLE

            btnUsarHuella.setOnClickListener {
                if (bloqueado) {
                    Toast.makeText(
                        requireContext(),
                        "Demasiados intentos fallidos, prueba luego",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    showBiometricPrompt()
                }
            }

            showBiometricPrompt()
        }

        binding.generateCodeButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()

            hideKeyboard()

            val preferences =
                requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)?.edit()

            preferences?.putString("correo", phoneNumber)
            preferences?.apply()

            if (phoneNumber.isNotEmpty()) {

                authViewModel.checkUserAccess(phoneNumber, requireActivity())

            } else {
                Toast.makeText(
                    requireContext(),
                    "Introduce un número de teléfono",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        authViewModel.accessGranted.observe(viewLifecycleOwner, Observer { hasAccess ->
            if (hasAccess == false) {
                Toast.makeText(
                    requireContext(),
                    "El usuario no tiene acceso o no está registrado",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        authViewModel.verificationId.observe(viewLifecycleOwner, Observer { verificationId ->
            val bundle = Bundle().apply {
                putString("verificationId", verificationId)
                putString("rol", authViewModel.rol.value)
            }
            findNavController().navigate(R.id.action_loginFragment_to_verifyFragment, bundle)
        })


        authViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })

        authViewModel.progress.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            binding.contPrincipal.isVisible = !isLoading
        })
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d("biometria", "error biometría: ${errString} code: $errorCode")

                    bloqueado = errorCode == 9
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    findNavController().navigate(R.id.action_loginFragment_to_listarFragment)
                }


            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Utiliza tu huella digital para acceder")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun isReVerificationNeeded(): Boolean {
        val lastVerification = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getLong("last_verification_time", 0)
        val currentTime = System.currentTimeMillis()
        val res = (currentTime - lastVerification) > 24 * 60 * 60 * 1000

        if (res) {
            authViewModel.cerrarSesion()
            requireContext().deleteSharedPreferences("auth")
        }

        return res
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun Fragment.hideKeyboard() {
        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.hideKeyboard()
        }
    }
}
