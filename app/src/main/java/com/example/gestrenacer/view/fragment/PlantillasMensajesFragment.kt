package com.example.gestrenacer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentPlantillasMensajesBinding
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.view.adapter.PlantillaAdapter
import com.example.gestrenacer.viewmodel.PlantillaViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class PlantillasMensajesFragment : Fragment() {

    private lateinit var binding: FragmentPlantillasMensajesBinding
    private val plantillaViewModel: PlantillaViewModel by viewModels()
    private lateinit var listaDePlantillas: MutableList<Plantilla>
    private lateinit var plantillaAdapter: PlantillaAdapter

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
        componentes()
        plantillaViewModel.obtenerPlantillas()
    }

    private fun componentes() {
        initializeRecyclerView()
        setupObservers()
        setupSwitchListener()
        btonCrearplantilla()


    }

    private fun initializeRecyclerView() {
        listaDePlantillas = mutableListOf()

        // Inicialización del adaptador sin el contexto y con el callback
        plantillaAdapter = PlantillaAdapter(listaDePlantillas) { plantilla ->
            onPlantillaClick(plantilla) // Manejar el clic en la plantilla
        }

        binding.recyclerViewPlantillas.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlantillas.adapter = plantillaAdapter
    }

    // Método para manejar clics en las plantillas
    private fun onPlantillaClick(plantilla: Plantilla) {
        // Aquí puedes manejar lo que sucede cuando se selecciona una plantilla
        Toast.makeText(context, "Plantilla seleccionada: ${plantilla.name}", Toast.LENGTH_SHORT).show()
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
                // Si el Switch está activado, muestra el botón para crear plantilla
                binding.etNombrePlantilla.visibility = View.VISIBLE
                binding.btnCrearPlantilla.visibility = View.VISIBLE
                binding.btnEnviar.visibility = View.GONE
            } else {
                // Si el Switch está desactivado, muestra el botón para enviar
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

            // Llamar al ViewModel para agregar la plantilla a la base de datos
            plantillaViewModel.crearPlantilla(nuevaPlantilla)

            // Limpiar campos después de crear la plantilla
            binding.etMensaje.text.clear()
            binding.etNombrePlantilla.text.clear()
        } else {
            // Mostrar un mensaje de error si los campos están vacíos
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }
}
