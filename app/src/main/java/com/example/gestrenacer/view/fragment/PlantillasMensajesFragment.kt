package com.example.gestrenacer.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.databinding.FragmentPlantillasMensajesBinding
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.view.adapter.GroupAdapter
import com.example.gestrenacer.view.adapter.PlantillaAdapter
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.viewmodel.PlantillaViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        setBackBtnUp()

    }


    private fun setBackBtnUp() {
        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun formatearFecha(timestamp: Timestamp): String{
        val date: Date = timestamp.toDate()
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    private fun initGroupsAutocomplete() {
        if (arguments?.getString("appliedFilters") == "false") {

            val autoCompleteTextView = binding.textViewGrupos
            val autoCompleteEditText = binding.groupsAutoCompleteTv

            autoCompleteTextView.visibility = View.VISIBLE
            autoCompleteEditText.visibility = View.VISIBLE

            groupViewModel.listaGroups.observe(viewLifecycleOwner) { groups ->
                val showDetailsDialog: (Group) -> Unit = { group ->
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(group.nombre)
                    var message = "Este grupo incluye las siguientes categorías:"
                    for (c in group.checkboxfilters) {
                        message += "\n - $c"
                    }
                    message += "\n\nY acota las edades en los siguientes rangos:\n"

                    var fechaInicial = "(Sin especificar)"
                    var fechaFinal = "(Sin especificar)"

                    val auxFechaInicial = group.datesfilters[0].toDate()
                    val auxFechaFinal = group.datesfilters[1].toDate()

                    if ((auxFechaInicial.date != 1) and (auxFechaInicial.month != 0) and (auxFechaInicial.year != 0)) {
                        fechaInicial = formatearFecha(group.datesfilters[0])
                    }

                    if ((auxFechaFinal.date != 0) and (auxFechaFinal.month != 12) and (auxFechaFinal.year != 300)) {
                        fechaFinal = formatearFecha(group.datesfilters[1])
                    }

                    message += "${fechaInicial} hasta ${fechaFinal}"

                    dialog.setMessage(message)
                    dialog.setNegativeButton("Cancelar", null)
                    dialog.setPositiveButton("Borrar") { _, _ ->
                        groupViewModel.deleteGroup(group)
                        groupViewModel.getGroups()
                    }
                    dialog.show()
                }

                val adapter = GroupAdapter(
                    context = requireContext(),
                    allGroups = groups,
                    autoCompleteTextView = autoCompleteEditText,
                    showDetailsDialog = showDetailsDialog
                )

                autoCompleteEditText.setAdapter(adapter)
            }

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
