package com.example.gestrenacer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.databinding.FragmentVerifySmsBinding
import com.example.gestrenacer.viewmodel.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyFragment : Fragment() {

    private var _binding: FragmentVerifySmsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var verificationId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerifySmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        verificationId = arguments?.getString("verificationId") ?: ""

        binding.verifyButton.setOnClickListener {
            val code = binding.verificationCodeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                hideKeyboard()
                authViewModel.signInWithCredential(PhoneAuthProvider.getCredential(verificationId, code))
            } else {
                Toast.makeText(requireContext(), "Introduce el código de verificación", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.authResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {

                Toast.makeText(requireContext(), "Verificación exitosa", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_verifyFragment_to_listarFragment)
            } else {
                Toast.makeText(requireContext(), "Error en la verificación", Toast.LENGTH_SHORT).show()
            }
        })


        authViewModel.progress.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
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
