package com.example.gestrenacer.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.databinding.FragmentPlantillasMensajesBinding
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.view.adapter.PlantillaAdapter
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.viewmodel.PlantillaViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class PlantillasMensajesFragment : Fragment() {

    private lateinit var binding: FragmentPlantillasMensajesBinding
    private val plantillaViewModel: PlantillaViewModel by viewModels()
    private lateinit var listaDePlantillas: MutableList<Plantilla>
    private lateinit var plantillaAdapter: PlantillaAdapter
    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlantillasMensajesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.getGroups()
        componentes()
        plantillaViewModel.obtenerPlantillas()
    }

    private fun componentes() {
        initGroupsAutocomplete()
        initializeRecyclerView()
        setupObservers()
        setupSwitchListener()
        btonCrearplantilla()


    }

    class GroupAdapter(
        context: Context,
        private val groups: List<String>,
        private val autoCompleteTextView: AutoCompleteTextView,
        private val onItemClick: (String) -> Unit,
        private val onItemLongClick: (String) -> Unit,
        private val showDetailsDialog: (String) -> Unit
    ) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, groups) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val groupName = getItem(position)

            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            view.setBackgroundResource(typedValue.resourceId)

            view.setOnClickListener {
                groupName?.let {
                    onItemClick(it)
                    autoCompleteTextView.setText(it, false)
                }
            }

            view.setOnLongClickListener {
                groupName?.let { showDetailsDialog(it) }
                true
            }

            return view
        }
    }

    private fun initGroupsAutocomplete() {
        val autoCompleteTextView = binding.groupsAutoCompleteTv
        groupViewModel.listaGroups.observe(viewLifecycleOwner) { groups ->
            val groupsList = groups.map { it.nombre }

            val showDetailsDialog: (String) -> Unit = { groupName ->
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(groupName)
                dialog.setMessage("Este grupo incluye las siguientes categorías:")
                dialog.setNegativeButton("Cancelar", null)
                dialog.setPositiveButton("Borrar") { _, _ ->
                    val itemsToDelete = groups.filter { it.nombre == groupName }
                    groupViewModel.deleteGroup(itemsToDelete[0])
                    groupViewModel.getGroups()
                }
                dialog.show()
            }

            val adapter = GroupAdapter(
                context = requireContext(),
                groups = groupsList,
                autoCompleteTextView = autoCompleteTextView,
                onItemClick = { groupName ->
                    Toast.makeText(requireContext(), "Selected: $groupName", Toast.LENGTH_SHORT).show()
                },
                onItemLongClick = { groupName ->
                    Toast.makeText(requireContext(), "Long pressed: $groupName", Toast.LENGTH_SHORT).show()
                },
                showDetailsDialog = showDetailsDialog
            )

            autoCompleteTextView.setAdapter(adapter)
        }
    }


    private fun initializeRecyclerView() {
        listaDePlantillas = mutableListOf()

        plantillaAdapter = PlantillaAdapter(listaDePlantillas,
            onPlantillaClick = { plantilla -> onPlantillaClick(plantilla) },
            onPlantillaLongClick = { plantilla -> showPlantillaDetailsDialog(plantilla) }
        )

        binding.recyclerViewPlantillas.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlantillas.adapter = plantillaAdapter

        val dividerItemDecoration = DividerItemDecoration(binding.recyclerViewPlantillas.context, LinearLayoutManager.VERTICAL)
        binding.recyclerViewPlantillas.addItemDecoration(dividerItemDecoration)
    }

    private fun onPlantillaClick(plantilla: Plantilla) {
        binding.etNombrePlantilla.setText(plantilla.name)
        binding.etMensaje.setText(plantilla.message)
    }

    private fun setupObservers() {
        plantillaViewModel.plantillas.observe(viewLifecycleOwner) { plantillas ->
            plantillas?.let {
                updatePlantillaList(it)
            }
        }
    }

    private fun updatePlantillaList(plantillas: List<Plantilla>) {
        listaDePlantillas.clear()
        listaDePlantillas.addAll(plantillas)
        plantillaAdapter.notifyDataSetChanged()
    }

    private fun setupSwitchListener() {
        binding.switchCrearPlantilla.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etNombrePlantilla.visibility = View.VISIBLE
                binding.btnCrearPlantilla.visibility = View.VISIBLE
                binding.btnEnviar.visibility = View.GONE
            } else {
                binding.etNombrePlantilla.visibility = View.GONE
                binding.btnCrearPlantilla.visibility = View.GONE
                binding.btnEnviar.visibility = View.VISIBLE
            }
        }
    }

    private fun btonCrearplantilla() {
        binding.btnCrearPlantilla.setOnClickListener {
            crearPlantilla()
        }
    }

    private fun crearPlantilla() {
        val mensaje = binding.etMensaje.text.toString().trim()
        val nombrePlantilla = binding.etNombrePlantilla.text.toString().trim()

        if (mensaje.isNotEmpty() && nombrePlantilla.isNotEmpty()) {
            // Crear objeto Plantilla con un ID único
            val nuevaPlantilla = Plantilla(
                id = UUID.randomUUID().toString(), // Genera un ID único
                name = nombrePlantilla,
                message = mensaje
            )

            plantillaViewModel.crearPlantilla(nuevaPlantilla)
            binding.etMensaje.text.clear()
            binding.etNombrePlantilla.text.clear()
        } else {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPlantillaDetailsDialog(plantilla: Plantilla) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(plantilla.name)
        dialog.setMessage(plantilla.message)
        dialog.setNegativeButton("Cancelar", null)
        dialog.setPositiveButton("Borrar") { _, _ ->
            // Crear una lista con la plantilla a eliminar
            val plantillasAEliminar = listOf(plantilla)

            // Lógica para borrar la plantilla
            plantillaViewModel.eliminarPlantillas(plantillasAEliminar) // Llama al método de eliminar en el ViewModel
        }
        dialog.show()
    }
}
