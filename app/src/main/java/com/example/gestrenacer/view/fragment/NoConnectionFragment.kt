package com.example.gestrenacer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentNoConnectionBinding
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoConnectionFragment : Fragment() {

    private val connectionViewModel: ConnectionViewModel by viewModels()
    private lateinit var binding: FragmentNoConnectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        binding.retryButton.setOnClickListener {
            connectionViewModel.checkConnection()
        }
    }

    private fun setupObservers() {
        connectionViewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                // Aquí puedes agregar lógica para ocultar el fragmento si hay conexión
                requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            }
        }
    }
}
