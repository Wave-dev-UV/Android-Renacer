package com.example.gestrenacer.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentPlantillasMensajesBinding
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.view.adapter.GroupAdapter
import com.example.gestrenacer.view.adapter.PlantillaAdapter
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.viewmodel.PlantillaViewModel
import com.example.gestrenacer.viewmodel.UserViewModel
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
    private val userViewModel: UserViewModel by viewModels()
    private var isProcessingMessage = false

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
        anadirRol()
        initGroupsAutocomplete()
        initializeRecyclerView()
        setupObservers()
        setupSwitchListener()
        btonCrearplantilla()
        btonEnviar()
        setBackBtnUp()
        setupTogglePlantillas()
    }

    private fun anadirRol() {
        val data = arguments?.getString("rol")
        userViewModel.colocarRol(data)
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
        if ((arguments?.getString("appliedFilters") == "false") and
            (arguments?.getString("rol") == "Administrador")) {

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
                        autoCompleteEditText.text.clear()
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

        plantillaViewModel.progresState.observe(viewLifecycleOwner) { isLoading ->
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

    private fun btonEnviar() {
        binding.btnEnviar.setOnClickListener {
            enviarMensaje()
        }
    }

    private fun btonCrearplantilla() {
        binding.btnCrearPlantilla.setOnClickListener {
            crearPlantilla()
        }
    }




    private fun enviarMensaje(): Int {
        if (isProcessingMessage) return 0  // Prevent multiple executions
        isProcessingMessage = true

        var result = 0
        if (arguments?.getString("appliedFilters") == "true") {
            result = 2
            isProcessingMessage = false
        } else if (arguments?.getString("rol") == "Administrador") {
            // Filtering using groups
            val selectedGroupName = binding.groupsAutoCompleteTv.text.toString()
            groupViewModel.listaGroups.observe(viewLifecycleOwner) { groups ->
                if (!isProcessingMessage) return@observe  // Skip if we're not processing

                val group = groups.find { e -> selectedGroupName == e.nombre }
                if (group != null) {
                    val checkBoxFilters = group.checkboxfilters
                    val datesFilters = group.datesfilters

                    val sexFilters = checkBoxFilters.filter { it in listOf("Masculino", "Femenino") }
                    val stateFilters = checkBoxFilters.filter {
                        it in listOf("Casado(a)", "Soltero(a)", "Divorciado(a)",
                            "Unión libre", "Viudo(a)")
                    }
                    val pendingFilters = listOf<String>()
                    val initialDate = datesFilters[0]
                    val finalDate = datesFilters[1]

                    Log.d("sendMessage - Sex Filters", sexFilters.toString())
                    Log.d("sendMessage - State Filters", stateFilters.toString())
                    Log.d("sendMessage - Pending Filters", pendingFilters.toString())
                    Log.d("sendMessage - Initial Date", initialDate.toString())
                    Log.d("sendMessage - Final Date", finalDate.toString())
                    result = 1
                } else {
                    Toast.makeText(context, "Ese grupo no existe. Elige uno válido",
                        Toast.LENGTH_SHORT).show()
                }
                isProcessingMessage = false
            }
        } else {
            isProcessingMessage = false
        }
        return result
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

            if (enviarMensaje() > 0) {
                // Comprobar si la plantilla es duplicada antes de crearla
                if (!plantillaViewModel.plantillaDuplicada(nuevaPlantilla.name)) {
                    plantillaViewModel.crearPlantilla(nuevaPlantilla)
                    binding.etMensaje.text.clear()
                    binding.etNombrePlantilla.text.clear()

                    // Mostrar mensaje de éxito
                    Toast.makeText(context, "Plantilla creada exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ya existe una plantilla con ese nombre", Toast.LENGTH_SHORT).show()
                }
            }

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

    private fun setupTogglePlantillas() {
        binding.tvTituloPlantillas.setOnClickListener {
            if (binding.recyclerViewPlantillas.visibility == View.GONE) {
                binding.recyclerViewPlantillas.visibility = View.VISIBLE
                binding.tvTituloPlantillas.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
            } else {
                binding.recyclerViewPlantillas.visibility = View.GONE
                binding.tvTituloPlantillas.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
            }
        }
    }


}

