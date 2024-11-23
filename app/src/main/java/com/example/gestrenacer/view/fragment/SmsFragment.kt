package com.example.gestrenacer

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.databinding.FragmentSmsBinding
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.utils.FechasAux
import com.example.gestrenacer.utils.FiltrosAux
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.adapter.GroupAdapter
import com.example.gestrenacer.view.adapter.PlantillaAdapter
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.viewmodel.PlantillaViewModel
import com.example.gestrenacer.viewmodel.SmsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import kotlin.time.Duration

@AndroidEntryPoint
class SmsFragment : Fragment() {

    private lateinit var binding: FragmentSmsBinding
    private val smsViewModel: SmsViewModel by viewModels()
    private val plantillaViewModel: PlantillaViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var listaDePlantillas: MutableList<Plantilla>
    private lateinit var plantillaAdapter: PlantillaAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.getGroups()
        iniciarComponentes()
        plantillaViewModel.obtenerPlantillas()
    }

    private fun iniciarComponentes() {
        manejadorTxtSms()
        manejadorBtnEnviar()
        manejadorBtnVolver()
        manejadorSwitchGuard()
        iniciarToolbar()
        iniciarFiltros()
        iniciarCantSms()
        iniciarUsuarios()
        initGroupsAutocomplete()
        initializeRecyclerView()
        observerExito()
        observerAwaiting()
        observerProgress()
        observerOperacion()
        observerGuardado()
        observerGrupoActivado()
        observerPlantillas()
        observerGuardPlantilla()
    }

    private fun iniciarToolbar() {
        val activity = requireActivity() as MainActivity
        activity.visibilidadBottomBar(false)
        binding.toolbar.lblToolbar.text = getString(R.string.lblMensajeria)
    }

    private fun iniciarFiltros() {
        val filtros = requireArguments().getStringArrayList("filtros") as ArrayList<String>

        for (i in filtros) {
            mostrarChip(i)
        }
    }

    private fun iniciarUsuarios() {
        val lista = requireArguments().getStringArrayList("usuarios")?.toMutableList()

        smsViewModel.iniciarUsuarios(lista as MutableList<String>)
    }

    private fun iniciarCantSms() {
        val cant = requireArguments().getStringArrayList("usuarios")?.size as Int

        if (cant > 0) {
            binding.lblPersonas.text = getString(R.string.txtEnvPersonSel) + " ${cant} personas."
        } else {
            binding.lblPersonas.text = getString(R.string.txtNoPerSms)
        }
    }

    private fun observerAwaiting() {
        smsViewModel.await.observe(viewLifecycleOwner) {
            if (it == 1) {
                smsViewModel.enviarSms(binding.txtSms.text.toString())
            }
        }
    }

    private fun observerGuardPlantilla() {
        plantillaViewModel.guardado.observe(viewLifecycleOwner) {
            when (it) {
                2 -> {
                    DialogUtils.dialogoInformativo(
                        requireContext(),
                        getString(R.string.titModalError),
                        getString(R.string.txtErrPlantilla),
                        getString(R.string.txtBtnAceptar)
                    ).show()
                    plantillaViewModel.cambiarGuardado(0)
                }
            }
        }
    }

    private fun observerGrupoActivado() {
        smsViewModel.grupoActivado.observe(viewLifecycleOwner) {
            binding.filtrosCont.isVisible = !it
            binding.lblFiltros.isVisible = !it
            binding.lblPersonas.isVisible = !it
        }
    }

    private fun observerGuardado() {
        smsViewModel.guardado.observe(viewLifecycleOwner) {
            if (it) binding.btnEnviar.text = getString(R.string.lblBtnEnvGuardSms)
            else binding.btnEnviar.text = getString(R.string.lblBtnEnviarSms)

            validarTexto()
        }
    }

    private fun observerProgress() {
        smsViewModel.progress.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.toolbar.root.isVisible = !it
            binding.btnEnviar.isVisible = !it
            binding.txtPlantilla.isVisible = !it
            binding.switchGuardSms.isVisible = !it
            binding.lblPlantillaGuard.isVisible = !it
            binding.lblGuardSms.isVisible = !it
            binding.contPrincipal.isVisible = !it
        }
    }

    private fun observerOperacion() {
        smsViewModel.operacion.observe(viewLifecycleOwner) {
            when (it) {
                2 -> {
                    DialogUtils.dialogoInformativo(
                        requireContext(),
                        getString(R.string.titModalError),
                        getString(R.string.txtErrorSms),
                        getString(R.string.txtBtnAceptar)
                    ).show()
                    smsViewModel.cambiarOperacion(0)
                }

                1 -> {
                    Toast.makeText(requireContext(),R.string.txtSmsEnviados,Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun observerPlantillas() {
        plantillaViewModel.plantillas.observe(viewLifecycleOwner) { plantillas ->
            plantillas?.let {
                updatePlantillaList(it)
            }
        }
    }

    private fun observerExito() {
        plantillaViewModel.exito.observe(viewLifecycleOwner) {
            val text = binding.txtSms.text?.isNotEmpty() as Boolean
            if (it && (plantillaViewModel.guardado.value == 0 && text)) {
                val existeGrupo = groupViewModel.listaGroups.value?.filter { x ->
                    x.nombre == binding.groupsAutoCompleteTv.text.toString()
                } as List<Group>

                enviarSms(existeGrupo, smsViewModel.grupoActivado.value as Boolean)
            }
        }
    }

    private fun mostrarChip(filtro: String) {
        val aux = filtro.split(" ")
        when (aux[0]) {
            "Casado(a)" -> binding.chipCasado.isVisible = true
            "Viudo(a)" -> binding.chipViudo.isVisible = true
            "Masculino" -> binding.chipMasculino.isVisible = true
            "Femenino" -> binding.chipFemenino.isVisible = true
            "Soltero(a)" -> binding.chipSoltero.isVisible = true
            "Unión" -> binding.chipLibre.isVisible = true
            "Divorciado(a)" -> binding.chipDivorciado.isVisible = true
            "Menor" -> {
                binding.chipEdadMin.setText("Menores de ${aux[1]} años.")
                binding.chipEdadMin.isVisible = true
            }

            "Mayor" -> {
                binding.chipEdadMax.setText("Mayores de ${aux[1]} años.")
                binding.chipEdadMax.isVisible = true
            }

            "Todas" -> binding.chipTodaEdad.isVisible = true
        }
    }

    private fun manejadorTxtSms() {
        val list = listOf(binding.txtSms, binding.txtPlantilla)

        list.forEach { i ->
            i.addTextChangedListener {
                validarTexto()
            }
        }
    }

    private fun validarTexto() {
        val cant = smsViewModel.usuarios.value?.size as Int
        val grupos = smsViewModel.grupoActivado.value as Boolean
        val plantilla = smsViewModel.guardado.value as Boolean
        val nomPlantilla = binding.txtPlantilla.text.toString().isNotEmpty()
        val txtSms = binding.txtSms.text.toString().isNotEmpty()
        binding.btnEnviar.isEnabled =
            (txtSms && (cant > 0 || grupos) && ((plantilla && nomPlantilla) || !plantilla))

        cambiarColorBtnEnviar()
    }

    private fun initGroupsAutocomplete() {
        val autoCompleteEditText = binding.groupsAutoCompleteTv

        autoCompleteEditText.visibility = View.VISIBLE

        groupViewModel.listaGroups.observe(viewLifecycleOwner) { groups ->
            val showDetailsDialog: (Group) -> Unit = { group ->
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(group.nombre)
                var message = "Este grupo incluye las siguientes categorías:\n"
                for (c in group.checkboxfilters) {
                    message += "\n - $c"
                }
                message += "\n\nAbarca "

                val auxFechaFinal = group.datesfilters[0].toDate()
                val auxFechaInicial = group.datesfilters[1].toDate()

                message += FechasAux.detTextoEdad(auxFechaInicial.year, auxFechaFinal.year)

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

        autoCompleteEditText.addTextChangedListener {
            smsViewModel.cambiarGrupoActivado(autoCompleteEditText.text.isNotEmpty())
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

        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerViewPlantillas.context,
            LinearLayoutManager.VERTICAL
        )
        binding.recyclerViewPlantillas.addItemDecoration(dividerItemDecoration)
    }

    private fun onPlantillaClick(plantilla: Plantilla) {
        binding.txtPlantilla.setText(plantilla.name)
        binding.txtSms.setText(plantilla.message)
    }

    private fun updatePlantillaList(plantillas: List<Plantilla>) {
        listaDePlantillas.clear()
        listaDePlantillas.addAll(plantillas)
        plantillaAdapter.notifyDataSetChanged()
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

    private fun crearPlantilla() {
        val mensaje = binding.txtSms.text.toString().trim()
        val nombrePlantilla = binding.txtPlantilla.text.toString().trim()
        // Crear objeto Plantilla con un ID único
        val nuevaPlantilla = Plantilla(
            id = UUID.randomUUID().toString(), // Genera un ID único
            name = nombrePlantilla,
            message = mensaje
        )
        plantillaViewModel.crearPlantilla(nuevaPlantilla)
    }

    private fun manejadorBtnVolver() {
        binding.toolbar.btnVolver.setOnClickListener {
            hideKeyboard()
            findNavController().popBackStack()
        }
    }

    private fun cambiarColorBtnEnviar() {
        if (binding.btnEnviar.isEnabled) {
            binding.btnEnviar.setBackgroundColor(resources.getColor(R.color.onSelectedColorBotBar))
        } else {
            binding.btnEnviar.setBackgroundColor(resources.getColor(R.color.btnDesactivado))
        }
    }

    private fun manejadorBtnEnviar() {
        binding.btnEnviar.setOnClickListener {
            val checked = binding.switchGuardSms.isChecked

            hideKeyboard()

            if (checked) {
                crearPlantilla()
            } else {
                val existeGrupo = groupViewModel.listaGroups.value?.filter { x ->
                    x.nombre == binding.groupsAutoCompleteTv.text.toString()
                } as List<Group>

                enviarSms(existeGrupo, smsViewModel.grupoActivado.value as Boolean)
            }
        }
    }

    private fun enviarSms(lista: List<Group>, grupo: Boolean) {
        if (grupo && lista.isEmpty()) {
            DialogUtils.dialogoInformativo(
                requireContext(),
                getString(R.string.titModalError),
                getString(R.string.txtGrupoErrNoEncontrado),
                getString(R.string.txtBtnAceptar)

            ).show()
        } else {
            if (grupo) {
                filtrarGrupo(lista)
            } else {
                smsViewModel.cambiarAwait(1)
            }
        }
    }

    private fun filtrarGrupo(lista: List<Group>) {
        val filtros = FiltrosAux.clasificarFiltros(
            lista[0].checkboxfilters,
            resources.getStringArray(R.array.listaEstadoCivil).toList(),
            resources.getStringArray(R.array.listaSexos).toList()
        )

        smsViewModel.obtenerMiembrosGrupo(
            lista[0].datesfilters[1], lista[0].datesfilters[0],
            filtros[1],
            filtros[0],
            resources.getStringArray(R.array.listaEstadoAtencion).toList()
        )
    }

    private fun manejadorSwitchGuard() {
        binding.switchGuardSms.setOnCheckedChangeListener { _, it ->
            binding.lblPlantilla.isEnabled = it
            binding.lblPlantilla.isVisible = it
            smsViewModel.cambiarGuardado(it)
        }
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun Fragment.hideKeyboard() {
        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.hideKeyboard()
        }
    }
}
