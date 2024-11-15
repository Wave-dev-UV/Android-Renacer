package com.example.gestrenacer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
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
                authViewModel.signInWithCredential(PhoneAuthProvider.getCredential(verificationId, code))
            } else {
                Toast.makeText(requireContext(), "Introduce el código de verificación", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.authResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_verifyFragment_to_listarFragment)
            } else {
                Toast.makeText(requireContext(), "Error en la verificación", Toast.LENGTH_SHORT).show()
            }
        })


        authViewModel.progress.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progress.isVisible = isLoading
            binding.contPrincipal.isVisible = !isLoading
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
