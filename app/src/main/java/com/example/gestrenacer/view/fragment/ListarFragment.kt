package com.example.gestrenacer.view.fragment

//import UserAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentListarFeligresesBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.adapter.UserAdapter
import com.example.gestrenacer.view.modal.ModalBottomSheet
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.Normalizer
import java.util.Date

@AndroidEntryPoint
class ListarFragment : Fragment() {
    private lateinit var binding: FragmentListarFeligresesBinding
    private val userViewModel: UserViewModel by viewModels()
    private var adapter: UserAdapter? = null
    private var userList = listOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListarFeligresesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.searchView.setQuery("",false)
        verFeligreses()
        forceRecyclerViewUpdate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()

        parentFragmentManager.setFragmentResultListener("editarUsuario", viewLifecycleOwner) { _, result ->
            val usuarioEditado = result.getBoolean("usuarioEditado", false)
            if (usuarioEditado) {
                verFeligreses()
                forceRecyclerViewUpdate()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        })
    }

    private fun iniciarComponentes(){
        anadirRol()
        observerListFeligreses()
        observerProgress()
        observerRol()
        configurarBusqueda()
        manejadorBtnAnadir()
        manejadorBtnMensaje()
        manejadorBottomBar()
        manejadorBtnFiltro()
        manejadorBtnEliminar()
        manejadorBtnCancelar()
        setupSelectAllCheckbox()

    }

    private fun verFeligreses(){
        val listEst = resources.getStringArray(R.array.listaEstadoCivil).toList()
        val listSexo = resources.getStringArray(R.array.listaSexos).toList()
        val listEstado = resources.getStringArray(R.array.listaEstadoAtencion).toList()

        userViewModel.getFeligreses(
            Timestamp(Date(0,1,0)),
            Timestamp(Date(300,12,0)),
            listEst, listSexo, listEstado
        )
    }

    private fun anadirRol() {
        val data = arguments?.getString("rol")
        userViewModel.colocarRol(data)
    }

    private fun observerListFeligreses() {
        userViewModel.listaUsers.observe(viewLifecycleOwner) { lista ->
            userList = lista
            binding.lblResultado.text = "Resultados: ${userList.size}"
            binding.txtNoResultados.isVisible = userList.isEmpty()

            if (adapter == null) {
                adapter = UserAdapter(userList, findNavController(), userViewModel.rol.value, userViewModel,
                    { isVisible -> binding.btnEliminar.isVisible = isVisible },
                    { selectedCount -> updateSelectedCountDisplay(selectedCount)
                    }
                )
                binding.listaFeligreses.layoutManager = LinearLayoutManager(context)
                binding.listaFeligreses.adapter = adapter

            } else {
                adapter?.updateList(userList)
            }
            // Actualiza la visualización de la selección al cargar la lista
            updateSelectedCountDisplay(adapter?.getSelectedUsersCount() ?: 0)
        }
    }

    private fun updateSelectedCountDisplay(selectedCount: Int) {
        binding.lblSeleccionados.text = "Seleccionados: $selectedCount"
        binding.lblSeleccionados.isVisible = selectedCount > 0
        binding.checkboxSelectAll.isVisible = selectedCount > 0
        binding.checkboxSelectAll.isChecked = selectedCount == userList.size

        // Mostrar los botones de acción si hay usuarios seleccionados
        if (selectedCount > 0) {
            binding.btnEliminar.isVisible = true
            binding.btnCancelar.isVisible = true
            binding.btnEnviarSms.isVisible = false // Ocultar cuando hay selecciones
            binding.btnAnadirFeligres.isVisible = false // Ocultar cuando hay selecciones
        } else {
            binding.btnEliminar.isVisible = false
            binding.btnCancelar.isVisible = false
            binding.btnEnviarSms.isVisible = true // Mostrar cuando no hay selecciones
            binding.btnAnadirFeligres.isVisible = true // Mostrar cuando no hay selecciones

        }
    }

    private fun observerProgress(){
        userViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it

            if (!it) {
                binding.listaFeligreses.visibility = View.VISIBLE
                reanudarBusqueda()
            }
            else{
                binding.listaFeligreses.visibility = View.INVISIBLE
            }
        }
    }

    private fun observerRol() {
        userViewModel.rol.observe(viewLifecycleOwner) { rol ->
            binding.btnAnadirFeligres.isVisible = rol in listOf("Administrador", "Gestor")
            binding.contBottomNav.isVisible = rol in listOf("Administrador", "Gestor")
            binding.btnEnviarSms.isVisible = rol in listOf("Administrador", "Gestor")
        }
    }

    private fun reanudarBusqueda(){
        if (binding.toolbar.searchView.query.isNotEmpty()){
            filter(binding.toolbar.searchView.query.toString())
        }
    }

    private fun manejadorBottomBar() {
        val bundle = Bundle()
        bundle.putString("rol",arguments?.getString("rol"))
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
                    Log.d("BottomNavSelect3", "Lista llamar deleccionado")
                    findNavController().navigate(R.id.action_listarFragment_to_pendingFragment, bundle)
                    true
                }
                else -> false
            }
        }
    }

    private fun manejadorBtnFiltro() {
        binding.btnFiltrar.setOnClickListener{
            val listFiltros = userViewModel.filtros.value as List<List<String>>
            val listOrden = userViewModel.orden.value as List<String>
            val modalBottomSheet = ModalBottomSheet(userViewModel::getFeligreses,
                listFiltros,listOrden)
            modalBottomSheet.show(requireActivity().supportFragmentManager,ModalBottomSheet.TAG)
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
            binding.toolbar.searchView.setQuery("",false)
            findNavController().navigate(R.id.action_listarFragment_to_agregarUsuariosFragment, bundle)
        }
    }

    private fun configurarBusqueda() {
        val searchView = binding.toolbar.searchView
        val closeButton: View? = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
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
                    adapter?.updateList(userList)
                    binding.lblResultado.text = "Resultados: ${userList.size}"
                } else {
                    filter(newText)
                }
                return false
            }
        })

        closeButton?.setOnClickListener {
            searchView.setQuery("",false)
            searchView.clearFocus()
            if (userList.size>0){
                binding.txtNoResultados.visibility = View.GONE
            }
        }

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

            val fullName = "$normalizedNombre $normalizedApellido"

            searchTerms.all { term ->
                normalizedNombre.contains(term, ignoreCase = true) ||
                        normalizedApellido.contains(term, ignoreCase = true) ||
                        fullName.contains(term, ignoreCase = true)
            }
        }

        adapter?.updateList(filteredList)
        binding.lblResultado.text = "Resultados: ${filteredList.size}"
        binding.txtNoResultados.isVisible = filteredList.isEmpty()
    }

    private fun forceRecyclerViewUpdate() {
        binding.listaFeligreses.layoutManager = LinearLayoutManager(context)
        binding.listaFeligreses.adapter = adapter
    }

    private fun setupSelectAllCheckbox() {
        binding.checkboxSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("YourFragment", "Checkbox 'Seleccionar Todos' presionado.")
            adapter?.selectAll(isChecked) // Selecciona o deselecciona según el estado del CheckBox
            binding.lblSeleccionados.text = "Seleccionados: ${adapter?.getSelectedUsersCount() ?: 0}" // Actualiza el texto
        }
    }
    private fun manejadorBtnEliminar() {
        binding.btnEliminar.setOnClickListener {
            eliminarSeleccionados()
        }
    }

    private fun contarAdministradores(): Int {
        return userList.count { it.rol == "Administrador" }
    }


    private fun eliminarSeleccionados() {
        val seleccionados = adapter?.getSelectedUsers() ?: return
        if (seleccionados.isEmpty()) return

        // Contar administradores actuales
        val adminCount = contarAdministradores()

        // Contar cuántos administradores se están eliminando
        val eliminandoAdmins = seleccionados.count { it.rol == "Administrador" }

        // Verificar si quedará al menos un administrador
        if (adminCount - eliminandoAdmins < 2) {
            AlertDialog.Builder(requireContext())
                .setTitle("Advertencia")
                .setMessage("Debes mantener al menos dos administradores en la lista.")
                .setPositiveButton("Ok", null)
                .show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar a los usuarios seleccionados?")
            .setPositiveButton("Sí") { _, _ ->
                userViewModel.eliminarUsuarios(seleccionados)
                adapter?.clearSelection()
                updateSelectedCountDisplay(0)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun manejadorBtnCancelar() {
        binding.btnCancelar.setOnClickListener {
            adapter?.clearSelection() // Método para deseleccionar
            updateSelectedCountDisplay(0) // Actualizar la visualización a 0 seleccionados
            adapter?.setLongPressMode(false)
        }
    }

}
