package com.example.gestrenacer.view.modal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.SheetFiltrosBinding
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.viewmodel.GroupViewModel
import com.example.gestrenacer.utils.FechasAux
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date

class ModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: SheetFiltrosBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var groupViewModel: GroupViewModel
    lateinit var  orden: List<String>
    lateinit var filtros: List<List<String>>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetFiltrosBinding.inflate(inflater)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.getGroups()
        orden = userViewModel.orden.value as List<String>
        filtros = userViewModel.filtros.value as List<List<String>>
        iniciarComponentes()
    }

    private fun iniciarComponentes(){
        iniciarCheckbox()
        iniciarText()
        iniciarRadioBtn()
        iniciarCreadorGrupos()
        manejadorTxt()
        manejadoresCheckBox()
        manejadoresRadioBtn()
        manejadorBtnFiltrar()
        manejadorBtnCerrar()
        manejadorCreadorDeGrupos()
    }

    private fun iniciarCreadorGrupos() {
        val pref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val roleMode = pref.getString("rol","Visualizador")

        if (roleMode == "Administrador") {
            binding.gruposTv.visibility = View.VISIBLE
            binding.groupEv.visibility = View.VISIBLE
            binding.swithGruposContainer.visibility = View.VISIBLE
        }
    }

    private fun iniciarText(){
        val anos = FechasAux.calcEdadLista(filtros[2])
        val edadFinal = anos[0]
        val edadInicial = anos[1]

        if (edadInicial > 0) {
            binding.txtEdadInicial.setText(edadInicial.toString())
        }

        if (edadFinal < 100) {
            binding.txtEdadFinal.setText(edadFinal.toString())
        }
    }

    private fun iniciarCheckbox(){
        val checkBox: MutableList<String> = mutableListOf()

        checkBox.addAll(filtros[0])
        checkBox.addAll(filtros[1])

        for (i in checkBox) {
            when (i) {
                "Soltero(a)" -> binding.checkSoltero.isChecked = true
                "Divorciado(a)" -> binding.checkDivorciado.isChecked = true
                "Unión libre" -> binding.checkLibre.isChecked = true
                "Viudo(a)" -> binding.checkViudo.isChecked = true
                "Casado(a)" -> binding.checkCasado.isChecked = true
                "Masculino" -> binding.checkMasculino.isChecked = true
                "Femenino" -> binding.checkFemenino.isChecked = true
            }
        }
    }

    private fun iniciarRadioBtn(){
        val res = "${orden[0]} ${orden[1]}"
        when (res){
            "nombre ascendente" -> {
                binding.radioBtnAlfabeticoAsc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroAlfabeticoAscen)
            }
            "nombre descendente" -> {
                binding.radioBtnAlfabeticoDesc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroAlfabeticoDescen)
            }
            "fechaNacimiento ascendente" -> {
                binding.radioBtnEdadAsc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroEdadAscen)
            }
            "fechaNacimiento descendente" -> {
                binding.radioBtnEdadDesc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroEdadDescen)
            }
            "fechaCreacion ascendente" -> {
                binding.radioBtnAntiguedadAsc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroAntiAscen)
            }
            "fechaCreacion descendente" -> {
                binding.radioBtnAntiguedadDesc.isChecked = true
                binding.lblCriterioOrden.text = getString(R.string.lblFiltroAntiDescen)
            }
        }
    }

    private fun manejadorTxt(){
        val txt = listOf(binding.txtEdadFinal,binding.txtEdadInicial)

        for (i in txt){
            i.addTextChangedListener{
                validarDatos()
            }
        }
    }

    private fun manejadorBtnFiltrar(){
        binding.btnAplicarFiltro.setOnClickListener{
            val createGroupToggle = binding.createGroupToggle
            val groupEvName = binding.groupEv.text.toString()

            fun executeFilter() {
                val listSexo = ArrayList<String>()
                val listEstado = ArrayList<String>()
                val listOrden = ArrayList<String>()
                var fechaInicial = Date().apply {
                    hours = 0
                    minutes = 0
                    seconds = 0
                }
                var fechaFinal = Date().apply {
                    hours = 0
                    minutes = 0
                    seconds = 0
                }

                if (binding.checkFemenino.isChecked) listSexo.add("Femenino")
                if (binding.checkMasculino.isChecked) listSexo.add("Masculino")
                if (binding.checkCasado.isChecked) listEstado.add("Casado(a)")
                if (binding.checkSoltero.isChecked) listEstado.add("Soltero(a)")
                if (binding.checkDivorciado.isChecked) listEstado.add("Divorciado(a)")
                if (binding.checkLibre.isChecked) listEstado.add("Unión libre")
                if (binding.checkViudo.isChecked) listEstado.add("Viudo(a)")

                when (binding.radioGroup.checkedRadioButtonId){
                    R.id.radioBtnAlfabeticoAsc -> listOrden.addAll(arrayOf("nombre","ascendente"))
                    R.id.radioBtnAlfabeticoDesc -> listOrden.addAll(arrayOf("nombre","descendente"))
                    R.id.radioBtnEdadAsc -> listOrden.addAll(arrayOf("fechaNacimiento","ascendente"))
                    R.id.radioBtnEdadDesc -> listOrden.addAll(arrayOf("fechaNacimiento","descendente"))
                    R.id.radioBtnAntiguedadAsc -> listOrden.addAll(arrayOf("fechaCreacion","ascendente"))
                    R.id.radioBtnAntiguedadDesc -> listOrden.addAll(arrayOf("fechaCreacion","descendente"))
                }

                if (binding.txtEdadFinal.text.isNotEmpty()){
                    fechaInicial.year -= binding.txtEdadFinal.text.toString().toInt() + 1
                }
                else{
                    fechaInicial.year = 0 //1900
                    fechaInicial.month = 0 //enero
                    fechaInicial.date = 1 //1
                }
                if (binding.txtEdadInicial.text.isNotEmpty()){
                    fechaFinal.year -= binding.txtEdadInicial.text.toString().toInt() - 1
                }
                else {
                    fechaFinal.year = 300 //2200
                    fechaFinal.month = 12 //diciembre
                    fechaFinal.date = 0 //21
                }

                val checkboxFilters: MutableList<String> = mutableListOf()

                for (e in listSexo) {
                    checkboxFilters.add(e)
                }
                for (e in listEstado) {
                    checkboxFilters.add(e)
                }

                if (createGroupToggle.isChecked) {
                    //esta alreves, mayor de a menor de
                    val groupWithFilters = Group(
                        nombre=groupEvName,
                        datesfilters = listOf(
                            Timestamp(fechaFinal),
                            Timestamp(fechaInicial)
                        ),
                        checkboxfilters = checkboxFilters
                    )

                    //groupViewModel.saveGroup(groupWithFilters)

                }

                userViewModel.getFeligreses(
                    Timestamp(fechaInicial), Timestamp(fechaFinal),
                    listEstado, listSexo, filtros[3], listOrden[0], listOrden[1])

                dismiss()

            }
            groupViewModel.listaGroups.observe(viewLifecycleOwner) { groups ->
                val existingName = groups.any { it.nombre == groupEvName }

                if (createGroupToggle.isChecked ) {
                    if ((groupEvName == "")) {
                        Toast.makeText(
                            context,
                            getString(R.string.por_favor_asigna_un_nombre_al_grupo),
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (existingName) {
                        Toast.makeText(
                            context,
                            getString(R.string.ya_hay_un_grupo_con_ese_nombre_elige_otro),
                            Toast.LENGTH_LONG
                        ).show()
                    }  else {
                        executeFilter()
                    }
                } else {
                    executeFilter()
                }
            }
        }
    }

    private fun manejadoresRadioBtn(){
        val list = listOf(binding.radioBtnEdadAsc, binding.radioBtnEdadDesc,
            binding.radioBtnAlfabeticoAsc, binding.radioBtnAlfabeticoDesc,
            binding.radioBtnAntiguedadAsc, binding.radioBtnAntiguedadDesc)

        for (i in list) {
            i.setOnClickListener {
                var text = ""
                when(i.id) {
                    R.id.radioBtnEdadAsc -> text=getString(R.string.lblFiltroEdadAscen)
                    R.id.radioBtnEdadDesc -> text=getString(R.string.lblFiltroEdadDescen)
                    R.id.radioBtnAlfabeticoAsc -> text=getString(R.string.lblFiltroAlfabeticoAscen)
                    R.id.radioBtnAlfabeticoDesc -> text=getString(R.string.lblFiltroAlfabeticoDescen)
                    R.id.radioBtnAntiguedadAsc -> text=getString(R.string.lblFiltroAntiAscen)
                    R.id.radioBtnAntiguedadDesc -> text=getString(R.string.lblFiltroAntiDescen)
                }
                binding.lblCriterioOrden.text = text
            }
        }
    }

    private fun manejadoresCheckBox(){
        val list = listOf(binding.checkCasado, binding.checkSoltero,
            binding.checkViudo,binding.checkLibre,binding.checkDivorciado,
            binding.checkMasculino,binding.checkFemenino)

        for (i in list) {
            i.setOnClickListener { validarDatos() }
        }
    }

    private fun manejadorBtnCerrar(){
        binding.btnCerrarFiltros.setOnClickListener{
            dismiss()
        }
    }

    private fun manejadorCreadorDeGrupos(){
        val toggleButton = binding.createGroupToggle
        toggleButton.setOnClickListener {
            if (toggleButton.isChecked) {
                binding.groupEv.isEnabled = true
            } else {
                binding.groupEv.isEnabled = false
            }
        }

    }

    private fun validarEdad(): Boolean{
        try {
            val txtInicial = binding.txtEdadInicial.text.toString()
            val txtFinal = binding.txtEdadFinal.text.toString()
            val vacio = txtInicial.isEmpty() && txtFinal.isEmpty()
            val ambosLlenosMax =
                txtInicial.isNotEmpty() && txtFinal.isNotEmpty() && (txtInicial.toInt() <= txtFinal.toInt())
            val ambosLlenosMin =
                txtInicial.isNotEmpty() && txtFinal.isNotEmpty() && (txtFinal.toInt() >= txtInicial.toInt())
            val llenoInicial = txtInicial.isNotEmpty() && txtFinal.isEmpty()
            val llenoFinal = txtFinal.isNotEmpty() && txtInicial.isEmpty()

            return vacio || (ambosLlenosMax && ambosLlenosMin) || llenoInicial || llenoFinal
        }
        catch (e: Exception){
            return false
        }
    }

    private fun validarDatos(){
        var color = resources.getColor(R.color.onSelectedColorBotBar)
        val listEstadoCheck = listOf(binding.checkCasado.isChecked, binding.checkSoltero.isChecked,
            binding.checkLibre.isChecked, binding.checkViudo.isChecked, binding.checkDivorciado.isChecked)
        val listSexoCheck = listOf(binding.checkFemenino.isChecked, binding.checkMasculino.isChecked)
        val activado = (listSexoCheck.reduce{acc, checkBox -> acc || checkBox } &&
                listEstadoCheck.reduce{acc, checkBox -> acc || checkBox} &&
                validarEdad())

        if (!activado) {
            color = resources.getColor(R.color.btnDesactivado)
        }

        binding.btnAplicarFiltro.setBackgroundColor(color)
        binding.btnAplicarFiltro.isEnabled = activado
    }

        companion object {
        const val TAG = "BottomSheetFiltros"
    }
}