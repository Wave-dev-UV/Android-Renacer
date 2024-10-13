package com.example.gestrenacer.view.modal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.SheetFiltrosBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Date

class ModalBottomSheet (
    private val filtrarFuncion: (List<List<String>>, Date, Date) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: SheetFiltrosBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetFiltrosBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()
    }

    private fun iniciarComponentes(){
        manejadoresCheckBox()
        manejadoresRadioBtn()
        manejadorBtnFiltrar()
        manejadorBtnCerrar()
    }

    private fun manejadorBtnFiltrar(){
        binding.btnAplicarFiltro.setOnClickListener{
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
            if (binding.checkCasado.isChecked) listEstado.add("Casado")
            if (binding.checkSoltero.isChecked) listEstado.add("Soltero")
            if (binding.radioBtnEdadAsc.isChecked) listOrden.add("Masculino")

            when (binding.radioGroup.checkedRadioButtonId){
                R.id.radioBtnAlfabeticoAsc -> listOrden.addAll(arrayOf("nombre","ascendente"))
                R.id.radioBtnAlfabeticoDesc -> listOrden.addAll(arrayOf("nombre","descendente"))
                R.id.radioBtnEdadAsc -> listOrden.addAll(arrayOf("fechaNacimiento","ascendente"))
                R.id.radioBtnEdadDesc -> listOrden.addAll(arrayOf("fechaNacimiento","descendente"))
            }

            if (binding.txtEdadFinal.text.isNotEmpty()){
                fechaInicial.year -= binding.txtEdadFinal.text.toString().toInt() + 1
            }
            if (binding.txtEdadInicial.text.isNotEmpty()){
                fechaFinal.year -= binding.txtEdadInicial.text.toString().toInt() - 1
            }

            filtrarFuncion(listOf(listSexo,listEstado,listOrden), fechaInicial, fechaFinal)
            dismiss()
        }
    }

    private fun manejadoresRadioBtn(){
        val list = listOf(binding.radioBtnEdadAsc, binding.radioBtnEdadDesc,
            binding.radioBtnAlfabeticoAsc, binding.radioBtnAlfabeticoDesc)

        for (i in list) {
            i.setOnClickListener {
                var text = ""
                when(i.id) {
                    R.id.radioBtnEdadAsc -> text=getString(R.string.lblFiltroEdadAscen)
                    R.id.radioBtnEdadDesc -> text=getString(R.string.lblFiltroEdadDescen)
                    R.id.radioBtnAlfabeticoAsc -> text=getString(R.string.lblFiltroAlfabeticoAscen)
                    R.id.radioBtnAlfabeticoDesc -> text=getString(R.string.lblFiltroAlfabeticoDescen)
                }
                binding.lblCriterioOrden.text = text
            }
        }
    }

    private fun manejadoresCheckBox(){
        val list = listOf(binding.checkCasado, binding.checkSoltero,
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

    private fun validarDatos(){
        var color = resources.getColor(R.color.onSelectedColorBotBar)
        val listEstadoCheck = listOf(binding.checkCasado.isChecked, binding.checkSoltero.isChecked)
        val listSexoCheck = listOf(binding.checkFemenino.isChecked, binding.checkMasculino.isChecked)
        val activado = (listSexoCheck.reduce{acc, checkBox -> acc || checkBox } &&
                listEstadoCheck.reduce{acc, checkBox -> acc || checkBox})

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