package com.example.gestrenacer.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
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

@AndroidEntryPoint
class PendingFragment : Fragment(), Recargable {
    private lateinit var binding: FragmentPendingBinding
    private lateinit var rol: String
    private val userViewModel: UserViewModel by viewModels()
    private var adapter: PendingUserAdapter? = null
    private var userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater)
        binding.lifecycleOwner = this
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
    }

    private fun iniciarComponentes(){
        observerListPendingFeligreses()
        observerProgress()
        anadirRol()
        configurarBusqueda()
        manejadorBtnFiltro()
    }

    override fun recargarDatos() {
        verFeligreses()
        forceRecyclerViewUpdate()
    }

    private fun verFeligreses(){
        val listEst = resources.getStringArray(R.array.listaEstadoCivil).toList()
        val listSexo = resources.getStringArray(R.array.listaSexos).toList()

        userViewModel.getFeligreses(
            Timestamp(Date(0,1,0)),
            Timestamp(Date(300,12,0)),
            listEst, listSexo, listOf("Por Llamar")
        )
    }

    private fun anadirRol() {
        val pref = activity?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador")

        val roles = pref in listOf("Administrador", "Gestor")
        val actividad = activity as MainActivity

        if (roles){
            actividad.visibilidadBottomBar(true)
        }

        if (pref == "Gestor"){
            actividad.modVisItemBottomBar(R.id.item_2,false)
        }

        rol = pref as String
    }

    private fun observerListPendingFeligreses(){
            userViewModel.listaUsers.observe(viewLifecycleOwner){
                userList = it

                if (adapter == null) {
                    adapter = PendingUserAdapter(userList, findNavController(),
                        rol, userViewModel,
                        this::setResSize, this::showNoContentMsg
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
            val listFiltros = userViewModel.filtros.value as List<List<String>>
            val listOrden = userViewModel.orden.value as List<String>
            val modalBottomSheet = ModalBottomSheet(userViewModel::getFeligreses,listFiltros,listOrden)
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
}