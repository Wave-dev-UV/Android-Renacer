package com.example.gestrenacer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentEditarUsuarioBinding
import com.example.gestrenacer.models.Feligres
import com.example.gestrenacer.view.dialog.DialogUtils
import com.example.gestrenacer.viewmodel.FeligresViewModel

class EditarUsuarioFragment : Fragment() {

    private lateinit var binding: FragmentEditarUsuarioBinding
    private val FeligresViewModel: FeligresViewModel by viewModels()
    private  lateinit var bundleFeligres: Feligres

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditarUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controlador()
    }

    private fun controlador(){
        manejarTipoId()
        manejarEstadoAtención()
        activarBoton()
        manejadorBtnVolver()
    }

    private fun activarBoton() {

        val listTxt = listOf(
            binding.editTextNombre,
            binding.editTextApellido,
            binding.editTextId,
            binding.editTextCelular,
            binding.editTextDireccion,
            binding.editTextEps,
            binding.editTextNombreContacto,
            binding.editTextCelularContacto,
            binding.editTextParentescoContacto,
            binding.editTextDireccionContacto,
            binding.autoCompleteTipoId
        )


        for (i in listTxt) {
            i.addTextChangedListener {
                validarCampos(listTxt)
            }
        }

        // Verifica si todos los campos están llenos
        validarCampos(listTxt)
    }

    private fun validarCampos(listTxt: List<View>) {
        val isFull = listTxt.all {
            when (it) {
                is AutoCompleteTextView -> it.text.toString().isNotEmpty()
                is EditText -> it.text.toString().isNotEmpty()
                else -> false
            }
        }

        binding.buttonEditar.isEnabled = isFull
    }

    private fun manejadorBtnVolver(){
        binding.imageButton.setOnClickListener{
            findNavController().popBackStack()
        }
    }

    private fun manejarTipoId() {
        val tiposDocumento = arrayOf("Cédula", "Tarjeta de Identidad", "Pasaporte")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposDocumento)
        binding.autoCompleteTipoId.setAdapter(adapter)

        var isDropdownVisible = false


        binding.autoCompleteTipoId.setOnClickListener {
            if (isDropdownVisible) {
                binding.autoCompleteTipoId.dismissDropDown()  // Ocultar el dropdown
            } else {
                binding.autoCompleteTipoId.showDropDown()  // Mostrar el dropdown
            }
            isDropdownVisible = !isDropdownVisible
        }


        binding.autoCompleteTipoId.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = tiposDocumento[position]

            isDropdownVisible = false  // Reiniciar cuando se selecciona un ítem
        }
    }


    private fun manejarEstadoAtención() {
        val tiposDocumento = arrayOf("Por Llamar", "En Proceso", "Llamado")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposDocumento)
        binding.autoCompleteEstadoAtencion.setAdapter(adapter)

        var isDropdownVisible = false


        binding.autoCompleteEstadoAtencion.setOnClickListener {
            if (isDropdownVisible) {
                binding.autoCompleteEstadoAtencion.dismissDropDown()  // Ocultar el dropdown
            } else {
                binding.autoCompleteEstadoAtencion.showDropDown()  // Mostrar el dropdown
            }
            isDropdownVisible = !isDropdownVisible
        }


        binding.autoCompleteTipoId.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = tiposDocumento[position]

            isDropdownVisible = false  // Reiniciar cuando se selecciona un ítem
        }
    }




}
