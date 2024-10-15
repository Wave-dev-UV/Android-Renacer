package com.example.gestrenacer.view.fragment

import UserAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentListarFeligresesBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.viewmodel.UserViewModel
import com.example.gestrenacer.view.modal.ModalBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.widget.SearchView
import java.text.Normalizer

@AndroidEntryPoint
class ListarFragment : Fragment() {
    private lateinit var binding: FragmentListarFeligresesBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private var userList = listOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListarFeligresesBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.getFeligreses()
        iniciarComponentes()
    }

    private fun iniciarComponentes() {
        anadirRol()
        observerListFeligreses()
        observerProgress()
        observerRol()
        configurarBusqueda()
        manejadorBtnAnadir()
        manejadorBtnMensaje()
        manejadorBottomBar()
        manejadorBtnFiltro()
    }

    private fun anadirRol() {
        val data = arguments?.getString("rol")
        userViewModel.colocarRol(data)
    }

    private fun observerListFeligreses() {
        userViewModel.listaUsers.observe(viewLifecycleOwner) { lista ->
            userList = lista.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() }))
            adapter = UserAdapter(userList, findNavController(), userViewModel.rol.value)
            binding.listaFeligreses.layoutManager = LinearLayoutManager(context)
            binding.listaFeligreses.adapter = adapter

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            })
        }
    }

    private fun observerProgress() {
        userViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
        }
    }

    private fun observerRol() {
        userViewModel.rol.observe(viewLifecycleOwner) {
            val data = arguments?.getString("rol")
            if (data in listOf("Administrador", "Gestor")) {
                binding.btnAnadirFeligres.visibility = View.VISIBLE
            }
            if (data == "Administrador") {
                binding.btnEnviarSms.visibility = View.VISIBLE
                binding.contBottomNav.visibility = View.VISIBLE
            }
        }
    }

    private fun manejadorBottomBar() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    Log.d("BottomNavSelect1", "Menú principal seleccionado")
                    true
                }
                R.id.item_2 -> {
                    Log.d("BottomNavSelect2", "Reportes seleccionado")
                    true
                }
                R.id.item_3 -> {
                    Log.d("BottomNavSelect3", "Lista llamar seleccionado")
                    true
                }
                else -> false
            }
        }
    }

    private fun manejadorBtnFiltro() {
        binding.btnFiltrar.setOnClickListener {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)
        }
    }

    private fun manejadorBtnMensaje() {
        binding.btnEnviarSms.setOnClickListener {
            Log.d("BtnSMS", "Clic en el botón de SMS")
        }
    }

    private fun manejadorBtnAnadir() {
        binding.btnAnadirFeligres.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("rol", userViewModel.rol.value)
            findNavController().navigate(R.id.action_listarFragment_to_agregarUsuariosFragment, bundle)
        }
    }

    private fun configurarBusqueda() {
        val searchView = requireView().findViewById<SearchView>(R.id.search_view)
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.clearFocus()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filter(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    adapter.updateList(userList.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() })))
                } else {
                    filter(newText)
                }
                return false
            }
        })
    }

    private fun filter(text: String) {
        val normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")

        val searchTerms = normalizedText.split(" ").filter { it.isNotEmpty() }

        val filteredList = userList.filter { user ->
            val normalizedNombre = Normalizer.normalize(user.nombre, Normalizer.Form.NFD)
                .replace("[^\\p{ASCII}]".toRegex(), "")
            val normalizedApellido = Normalizer.normalize(user.apellido, Normalizer.Form.NFD)
                .replace("[^\\p{ASCII}]".toRegex(), "")

            when (searchTerms.size) {
                1 -> normalizedNombre.contains(searchTerms[0], ignoreCase = true) ||
                        normalizedApellido.contains(searchTerms[0], ignoreCase = true)
                2 -> normalizedNombre.contains(searchTerms[0], ignoreCase = true) &&
                        normalizedApellido.contains(searchTerms[1], ignoreCase = true)
                else -> {
                    val fullName = "$normalizedNombre $normalizedApellido"
                    fullName.contains(normalizedText, ignoreCase = true)
                }
            }
        }.sortedWith(compareBy({ it.nombre.lowercase() }, { it.apellido.lowercase() }))

        adapter.updateList(filteredList)
    }
}
