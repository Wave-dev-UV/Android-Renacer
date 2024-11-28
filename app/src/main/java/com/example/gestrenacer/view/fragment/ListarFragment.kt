package com.example.gestrenacer.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentListarFeligresesBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.FechasAux
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.MainActivity.Recargable
import com.example.gestrenacer.view.adapter.UserAdapter
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.view.modal.ModalBottomSheet
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.Normalizer
import java.util.Calendar
import java.util.Date


@AndroidEntryPoint
class ListarFragment : Fragment(), Recargable {
    private lateinit var binding: FragmentListarFeligresesBinding
    private lateinit var rol: String
    private lateinit var userViewModel: UserViewModel
    private lateinit var groupViewModel: GroupViewModel
    private var adapter: UserAdapter? = null
    private var userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListarFeligresesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.searchView.setQuery("", false)
        cargarFiltros()
        forceRecyclerViewUpdate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()

        parentFragmentManager.setFragmentResultListener(
            "editarUsuario",
            viewLifecycleOwner
        ) { _, result ->
            val usuarioEditado = result.getBoolean("usuarioEditado", false)
            if (usuarioEditado) {
                cargarFiltros()
                forceRecyclerViewUpdate()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    private fun iniciarComponentes() {
        anadirRol()
        observerListFeligreses()
        observerProgress()
        observerMostrarFiltros()
        configurarBusqueda()
        manejadorBtnAnadir()
        manejadorBtnMensaje()
        manejadorBtnFiltro()
        manejadorBtnEliminar()
        manejadorBtnCancelar()
        setupSelectAllCheckbox()

    }

    override fun recargarDatos() {
        cargarFiltros()
        anadirRol()
        forceRecyclerViewUpdate()
    }

    private fun observerMostrarFiltros() {
        userViewModel.mostrarFiltros.observe(viewLifecycleOwner) {
            if (it) {
                val modalBottomSheet = ModalBottomSheet()
                modalBottomSheet.show(
                    requireActivity().supportFragmentManager,
                    ModalBottomSheet.TAG
                )
                userViewModel.cambiarMostFiltros(false)
            }
        }
    }

    private fun observerListFeligreses() {
        userViewModel.listaUsers.observe(viewLifecycleOwner) { lista ->
            userList = lista
            binding.lblResultado.text = "Resultados: ${userList.size}"
            binding.txtNoResultados.isVisible = userList.isEmpty()

            if (adapter == null) {
                adapter = UserAdapter(
                    userList,
                    findNavController(),
                    rol,
                    userViewModel,
                    { isVisible -> binding.btnEliminar.isVisible = isVisible },
                    { selectedCount ->
                        updateSelectedCountDisplay(selectedCount)
                    },
                    this::guardarFiltros
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

        binding.contenedorSeleccionados.isVisible = selectedCount > 0

        // Mostrar/ocultar botones según si hay usuarios seleccionados
        val hasSelectedUsers = selectedCount > 0
        binding.btnEliminar.isVisible = hasSelectedUsers
        binding.btnEnviarSms.isVisible = (rol == "Administrador") && (selectedCount == 0)
        binding.btnAnadirFeligres.visibility = (
                if (!hasSelectedUsers && (rol in listOf("Administrador", "Gestor"))) View.VISIBLE
                else if (hasSelectedUsers && (rol in listOf(
                        "Administrador",
                        "Gestor"
                    ))
                ) View.INVISIBLE
                else View.GONE
                )

        val shouldHideFiltersAndSearch = hasSelectedUsers
        binding.contenedorFiltros.isVisible =
            !shouldHideFiltersAndSearch // Oculta si hay seleccionados, muestra si no
    }

    private fun observerProgress() {
        userViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it

            if (!it) {
                reanudarBusqueda()
            }
        }
    }

    private fun anadirRol() {
        val pref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador") as String
        val actividad = activity as MainActivity


        if (pref == "Gestor" || pref == "Visualizador") {
            actividad.modVisItemBottomBar(R.id.item_2, false)
        }

        rol = pref
    }

    private fun reanudarBusqueda() {
        if (binding.toolbar.searchView.query.isNotEmpty()) {
            filter(binding.toolbar.searchView.query.toString())
        }
    }

    private fun manejadorBtnFiltro() {
        binding.btnFiltrar.setOnClickListener {
            userViewModel.cambiarMostFiltros(true)
        }
    }

    private fun manejadorBtnMensaje() {
        binding.btnEnviarSms.setOnClickListener {
            val listFiltros = userViewModel.filtros.value as List<List<String>>
            val edades = FechasAux.calcEdadLista(listFiltros[2])
            val lista = listFiltros[0] + listFiltros[1] + detEdad(edades)

            val bundle = Bundle().apply {
                val array1 = ArrayList<String>()
                val array2 = ArrayList<String>()

                array1.addAll(lista)
                putStringArrayList("filtros", array1)

                array2.addAll(anadirFeligresBundle())

                putStringArrayList("usuarios", array2)
            }

            guardarFiltros()
            findNavController().navigate(R.id.action_listarFragment_to_smsFragment, bundle)
        }
    }

    private fun manejadorBtnAnadir() {
        binding.btnAnadirFeligres.setOnClickListener {
            binding.toolbar.searchView.setQuery("", false)
            guardarFiltros()
            findNavController().navigate(
                R.id.action_listarFragment_to_agregarUsuariosFragment
            )
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
            searchView.setQuery("", false)
            searchView.clearFocus()
            if (userList.size > 0) {
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
            adapter?.selectAll(isChecked) // Selecciona o deselecciona según el estado del CheckBox
            binding.lblSeleccionados.text =
                "Seleccionados: ${adapter?.getSelectedUsersCount() ?: 0}" // Actualiza el texto
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

    private fun verAutoborrado(list: List<User>): Int {
        val preferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val tel = preferences?.getString("telefono", "")

        return list.count { it.celular == tel }
    }

    private fun eliminarSeleccionados() {
        val seleccionados = adapter?.getSelectedUsers() ?: return
        if (seleccionados.isEmpty()) return

        val autoborrado = verAutoborrado(seleccionados) != 0
        val adminCount = contarAdministradores()
        val eliminandoAdmins = seleccionados.count { it.rol == "Administrador" }

        if (adminCount - eliminandoAdmins < 1) {
            DialogUtils.dialogoInformativo(
                requireContext(),
                getString(R.string.titModalError),
                getString(R.string.txtErrorBorrarAdmin),
                getString(R.string.txtBtnAceptar)
            ).show()
        } else if (autoborrado) {
            DialogUtils.dialogoInformativo(
                requireContext(),
                getString(R.string.titModalError),
                getString(R.string.txtErrorBorrarTodos),
                getString(R.string.txtBtnAceptar)
            ).show()
        } else {
            DialogUtils.dialogoConfirmacion(
                context = requireContext(),
                mensaje = getString(R.string.txtModalEliminar),
                onYes = {
                    binding.toolbar.searchView.setQuery("",true)
                    userViewModel.eliminarUsuarios(seleccionados.toMutableList())
                    adapter?.clearSelection()
                    updateSelectedCountDisplay(0)
                }
            )
        }
    }


    private fun manejadorBtnCancelar() {
        binding.iconCancelar.setOnClickListener {
            adapter?.clearSelection() // Método para deseleccionar
            updateSelectedCountDisplay(0) // Actualizar la visualización a 0 seleccionados
            adapter?.setLongPressMode(false)
        }
    }

    private fun detEdad(edades: List<Int>): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        if (edades[0] < 100) {
            list.add("Menor ${edades[0]}")
        }
        if (edades[1] > 0) {
            list.add("Mayor ${edades[1]}")
        }
        if (edades[1] <= 0 && edades[0] >= 100) {
            list.add("Todas")
        }

        return list
    }

    private fun anadirFeligresBundle(): List<String> {
        val itemCount = adapter?.getSelectedUsersCount() as Int

        if (itemCount > 0) {
            return adapter?.getSelectedUsers()?.map { x -> x.celular } as List<String>
        } else {
            return userList.map { x -> x.celular }
        }
    }

    private fun guardarFiltros() {
        val conjuntoFiltros: MutableSet<String> = mutableSetOf()

        userViewModel.filtros.value?.forEach { x -> conjuntoFiltros.addAll(x) }

        val preferences =
            requireActivity().getSharedPreferences("filtros", Context.MODE_PRIVATE).edit()

        preferences.putStringSet("filtros", conjuntoFiltros)
        preferences.putStringSet("orden", userViewModel.orden.value?.toMutableSet())

        preferences.apply()
    }

    private fun cargarFiltros() {
        val listEst = resources.getStringArray(R.array.listaEstadoCivil).toSet()
        val listSexo = resources.getStringArray(R.array.listaSexos).toSet()
        val listEstado = resources.getStringArray(R.array.listaEstadoAtencion).toSet()
        try {
            val preferences =
                requireActivity().getSharedPreferences("filtros", Context.MODE_PRIVATE)
            val filtros = preferences.getStringSet("filtros", mutableSetOf()) as MutableSet<String>
            val orden =
                cargarOrden(preferences.getStringSet("orden", mutableSetOf()) as MutableSet<String>)

            if (orden == listOf("", "")) throw Exception()

            val edades = filtros - listSexo - listEst - listEstado
            val filtroSexo = filtros - listEst - edades - listEstado
            val filtroEst = filtros - listSexo - edades - listEstado
            val calendar = Calendar.getInstance()

            val aux = edades.map { x -> x.toInt() }.sorted()

            userViewModel.getFeligreses(
                Timestamp(Date(aux[0], calendar.time.month, calendar.time.date)),
                Timestamp(Date(aux[1], calendar.time.month, calendar.time.date)),
                filtroEst.toList(), filtroSexo.toList(), listEstado.toList(),
                orden[0], orden[1]
            )
        } catch (e: Exception) {
            userViewModel.getFeligreses(
                Timestamp(Date(0, 1, 0)),
                Timestamp(Date(300, 12, 0)),
                listEst.toList(), listSexo.toList(), listEstado.toList()
            )

        }
    }

    private fun cargarOrden(conjunto: MutableSet<String>): List<String> {
        val list: MutableList<String> = mutableListOf("", "")
        for (i in conjunto) {
            when (i) {
                "nombre", "fechaNacimiento", "fechaCreacion" -> list[0] = i
                "ascendente", "descendente" -> list[1] = i
            }
        }

        return list
    }
}