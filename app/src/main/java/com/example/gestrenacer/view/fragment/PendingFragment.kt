package com.example.gestrenacer.view.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentPendingBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.adapter.PendingUserAdapter
import com.example.gestrenacer.view.modal.ModalBottomSheet
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.Normalizer
import java.util.Date
import com.example.gestrenacer.view.MainActivity.Recargable
import com.example.gestrenacer.viewmodel.GroupViewModel
import java.util.Calendar

@AndroidEntryPoint
class PendingFragment : Fragment(), Recargable {
    private lateinit var binding: FragmentPendingBinding
    private lateinit var rol: String
    private lateinit var userViewModel: UserViewModel
    private lateinit var groupViewModel: GroupViewModel
    private var adapter: PendingUserAdapter? = null
    private var appliedFilters = false
    private var userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater)
        binding.lifecycleOwner = this
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.searchView.setQuery("",false)
        cargarFiltros()
        forceRecyclerViewUpdate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()

        parentFragmentManager.setFragmentResultListener("editarUsuario", viewLifecycleOwner) { _, result ->
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

    private fun iniciarComponentes(){
        observerListPendingFeligreses()
        observerProgress()
        anadirRol()
        configurarBusqueda()
        manejadorBtnFiltro()
    }

    override fun recargarDatos() {
        cargarFiltros()
        anadirRol()
        forceRecyclerViewUpdate()
    }

    private fun anadirRol() {
        val pref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador")

        val actividad = activity as MainActivity


        actividad.visibilidadBottomBar(true)


        if (pref == "Gestor" || pref == "Visualizador"){
            actividad.modVisItemBottomBar(R.id.statsFragment,false)
        }

        rol = pref as String
    }

    private fun observerListPendingFeligreses(){
            userViewModel.listaUsers.observe(viewLifecycleOwner){
                if (it != null) {
                    userList = it as MutableList<User>
                }

                if (adapter == null) {
                    adapter = PendingUserAdapter(userList, findNavController(),
                        rol, userViewModel,
                        this::setResSize, this::showNoContentMsg,
                        this::guardarFiltros
                    )
                    binding.listaFeligreses.layoutManager = LinearLayoutManager(context)
                    binding.listaFeligreses.adapter = adapter
                } else {
                    adapter?.updateList(userList)
                }

                setResSize(userList)
                showNoContentMsg(userList)
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

    private fun manejadorBtnFiltro() {
        binding.btnFiltrar.setOnClickListener{
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)
        }
    }

    private fun reanudarBusqueda(){
        if (binding.toolbar.searchView.query.isNotEmpty()){
            filter(binding.toolbar.searchView.query.toString())
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
            showNoContentMsg(userList)
            if (userList.size>0){
                binding.noDataMessage.visibility = View.GONE
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
        }.toMutableList()

        adapter?.updateList(filteredList,userList)
        binding.lblResultado.text = "Resultados: ${filteredList.size}"

        if (filteredList.isEmpty()) {
            binding.noDataMessage.visibility = View.VISIBLE
        } else {
            binding.noDataMessage.visibility = View.GONE
        }
    }

    private fun forceRecyclerViewUpdate() {

        binding.listaFeligreses.layoutManager = LinearLayoutManager(context)
        binding.listaFeligreses.adapter = adapter
    }

    val setAppliedFilters: (Boolean) -> Unit = { x -> appliedFilters = x }

    private fun showNoContentMsg(userList: List<User>){
        if (userList.isEmpty()) {
            binding.noDataMessage.visibility = View.VISIBLE
        } else {
            binding.noDataMessage.visibility = View.GONE
        }
    }

    private fun setResSize(userList: List<User>){
        binding.lblResultado.text = "Resultados: ${userList.size}"
    }

    private fun guardarFiltros() {
        val conjuntoFiltros: MutableSet<String> = mutableSetOf()

        userViewModel.filtros.value?.forEach { x -> conjuntoFiltros.addAll(x) }

        val preferences =
            requireActivity().getSharedPreferences("filtrosPending", Context.MODE_PRIVATE).edit()

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
                requireActivity().getSharedPreferences("filtrosPending", Context.MODE_PRIVATE)
            val filtros = preferences.getStringSet("filtros", mutableSetOf()) as MutableSet<String>
            val orden =
                cargarOrden(preferences.getStringSet("orden", mutableSetOf()) as MutableSet<String>)

            if (orden == listOf("","")) throw Exception()

            val edades = filtros - listSexo - listEst - listEstado
            val filtroSexo = filtros - listEst - edades - listEstado
            val filtroEst = filtros - listSexo - edades - listEstado
            val calendar = Calendar.getInstance()

            val aux = edades.map { x -> x.toInt() }.sorted()

            userViewModel.getFeligreses(
                Timestamp(Date(aux[0], calendar.time.month, calendar.time.date)),
                Timestamp(Date(aux[1], calendar.time.month, calendar.time.date)),
                filtroEst.toList(), filtroSexo.toList(), listOf("Por Llamar"),
                orden[0], orden[1]
            )
        } catch (e: Exception) {
            userViewModel.getFeligreses(
                Timestamp(Date(0, 1, 0)),
                Timestamp(Date(300, 12, 0)),
                listEst.toList(), listSexo.toList(), listOf("Por Llamar")
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