package com.example.gestrenacer.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentPendingBinding
import com.example.gestrenacer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendingFragment : Fragment() {
    private lateinit var binding: FragmentPendingBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.getFeligreses()
        iniciarComponentes()
    }

    private fun iniciarComponentes(){
        anadirRol()
        bottomNav()
        observerRol()
        manejadorBottomBar()
    }

    private fun anadirRol(){
        val data = arguments?.getString("rol")
        userViewModel.colocarRol(data)
    }

    private fun observerRol() {
        userViewModel.rol.observe(viewLifecycleOwner) {
            val data = arguments?.getString("rol")
            if (data == "Administrador") {
                binding.contBottomNav.visibility = View.VISIBLE
            }
        }
    }

    private fun manejadorBottomBar() {
        val bundle = Bundle()
        bundle.putString("rol",arguments?.getString("rol"))
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    Log.d("BottomNavSelect1", "Menú principal seleccionado")
                    findNavController().navigate(R.id.action_pendingFragment_to_listarFragment, bundle)
                    true
                }
                R.id.item_2 -> {
                    Log.d("BottomNavSelect2", "Reportes seleccionado")
                    findNavController().navigate(R.id.action_pendingFragment_to_listarFragment, bundle)
                    true
                }
                R.id.item_3 -> {
                    Log.d("BottomNavSelect3", "Lista llamar deleccionado")
                    true
                }
                else -> false
            }
        }
    }

    private fun bottomNav() {
        binding.bottomNavigation.menu.findItem(R.id.item_3).setChecked(true);
    }

}