package com.example.gestrenacer

import android.content.Context
import android.content.Intent
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
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    private var bloqueado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClientId))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInClient.signOut()
        authViewModel.cerrarSesion()

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), getString(R.string.txtErrorGoogle),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun iniciarComponentes(){
        observerProgress()
        observerAuthResult()
        manejadorBtnIniciarSesion()
        verificarHuella()
    }

    private fun observerAuthResult(){
        authViewModel.rol.observe(viewLifecycleOwner){
            if (it.isNotEmpty() && it != "Feligrés") {
                val preferences = requireContext().getSharedPreferences("auth",Context.MODE_PRIVATE)?.edit()

                preferences?.putBoolean("user_verified",true)
                preferences?.putLong("last_verification_time",System.currentTimeMillis())
                preferences?.putString("rol",authViewModel.rol.value)
                preferences?.apply()

                findNavController().navigate(R.id.action_loginFragment_to_listarFragment)
            }
            else if (it.isNotEmpty() && it == "Feligrés"){
                googleSignInClient.signOut()
                authViewModel.cerrarSesion()
                Toast.makeText(requireContext(),getString(R.string.txtErrorAut),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observerProgress(){
        authViewModel.progress.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progress.isVisible = isLoading
            binding.contPrincipal.isVisible = !isLoading
        })
    }

    private fun verificarHuella(){
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
    }

    private fun manejadorBtnIniciarSesion(){
        binding.btnLogin.setOnClickListener {

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    authViewModel.verificarAcceso(user?.email as String)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.txtErrorGoogle),Toast.LENGTH_LONG).show()
                }
            }
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
            googleSignInClient.signOut()
            authViewModel.cerrarSesion()
            requireContext().deleteSharedPreferences("auth")
        }

        return res
    }
}
