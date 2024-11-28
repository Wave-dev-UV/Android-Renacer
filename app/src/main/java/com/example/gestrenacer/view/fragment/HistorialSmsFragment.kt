package com.example.gestrenacer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.gestrenacer.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.databinding.FragmentHistorialSmsBinding
import com.example.gestrenacer.view.MainActivity.Recargable
import com.example.gestrenacer.view.adapter.SmsAdapter
import com.example.gestrenacer.viewmodel.SmsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistorialSmsFragment: Fragment(), Recargable {
    private lateinit var binding: FragmentHistorialSmsBinding
    private val smsViewModel: SmsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistorialSmsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        smsViewModel.verSms()
        iniciarComponentes()
    }

    private fun iniciarComponentes(){
        observerHistorial()
        observerProgress()
        manejadorBtnToolbar()
    }

    private fun manejadorBtnToolbar(){
        binding.toolbar.lblToolbar.text = getString(R.string.lblHistSms)
        binding.toolbar.btnVolver.setOnClickListener{
            findNavController().popBackStack()
        }
    }

    private fun observerHistorial(){
        smsViewModel.listaSms.observe(viewLifecycleOwner){
            val recyclerView = binding.listaSms
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = SmsAdapter(it)
            recyclerView.adapter = adapter

            binding.txtNoResultados.isVisible = it.isEmpty()
        }
    }

    private fun observerProgress(){
        smsViewModel.progress.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.listaSms.isVisible = !it
            binding.toolbar.root.isVisible = !it

        }
    }

    override fun recargarDatos() {
        smsViewModel.verSms()
    }
}