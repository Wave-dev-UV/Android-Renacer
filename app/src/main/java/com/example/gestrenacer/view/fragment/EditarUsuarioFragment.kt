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

        // Simular un objeto Feligres
        val pruebaFeligres = Feligres(
            nombre = "Juan",
            apellido = "Pérez",
            id = "123456",
            firestoreId = "firestoreId123",
            tipoId = "Cédula",
            celular = "123456789",
            direccion = "Calle Falsa 123",
            eps = "EPS Salud",
            nombreContacto = "Maria",
            celularContacto = "987654321",
            parentescoContacto = "Hermana",
            direccionContacto = "Calle Verdadera 456",
            esLider = true,
            tieneAcceso = false,
            estadoAtencion = "En Proceso"
        )

        // Simular el Bundle
        val bundle = Bundle().apply {
            putSerializable("dataFeligres", pruebaFeligres)
        }
        arguments = bundle

        // Llama a la función que inicializa los campos
        controlador()
    }




    private lateinit var firestoreId: String

    private fun controlador(){
        inicializarFeligres()
        manejarTipoId()
        manejarEstadoAtencion()
        activarBoton()
        manejadorBtnVolver()
        manejadorBtnEditar()
    }

    private fun inicializarFeligres() {
        val receivedBundle = arguments

        if (receivedBundle != null) {
            bundleFeligres = receivedBundle.getSerializable("dataFeligres") as Feligres

            binding.editTextNombre.setText(bundleFeligres.nombre)
            binding.editTextApellido.setText(bundleFeligres.apellido)
            binding.editTextId.setText(bundleFeligres.id)
            binding.editTextCelular.setText(bundleFeligres.celular)
            binding.editTextDireccion.setText(bundleFeligres.direccion)
            binding.editTextEps.setText(bundleFeligres.eps)
            binding.editTextNombreContacto.setText(bundleFeligres.nombreContacto)
            binding.editTextCelularContacto.setText(bundleFeligres.celularContacto)
            binding.editTextParentescoContacto.setText(bundleFeligres.parentescoContacto)
            binding.editTextDireccionContacto.setText(bundleFeligres.direccionContacto)


            binding.autoCompleteTipoId.setText(bundleFeligres.tipoId)
            binding.switch1.isChecked = bundleFeligres.esLider
            binding.switch2.isChecked = bundleFeligres.tieneAcceso
            binding.autoCompleteEstadoAtencion.setText(bundleFeligres.estadoAtencion)

            firestoreId = bundleFeligres.firestoreId
        }
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


    private fun manejarEstadoAtencion() {
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

    private fun manejadorBtnEditar() {
        binding.buttonEditar.setOnClickListener {
            DialogUtils.dialogoEditConfirmacion(requireContext()) {
                updateFeligres()
            }
        }
    }

    private fun updateFeligres() {
        val feligresActualizado = Feligres(
            nombre = binding.editTextNombre.text.toString(),
            apellido = binding.editTextApellido.text.toString(),
            id = binding.editTextId.text.toString(),
            tipoId = binding.autoCompleteTipoId.text.toString(),
            celular = binding.editTextCelular.text.toString(),
            direccion = binding.editTextDireccion.text.toString(),
            eps = binding.editTextEps.text.toString(),
            nombreContacto = binding.editTextNombreContacto.text.toString(),
            celularContacto = binding.editTextCelularContacto.text.toString(),
            parentescoContacto = binding.editTextParentescoContacto.text.toString(),
            direccionContacto = binding.editTextDireccionContacto.text.toString(),
            esLider = binding.switch1.isChecked,
            tieneAcceso = binding.switch2.isChecked,
            estadoAtencion = binding.autoCompleteEstadoAtencion.text.toString(),
            firestoreId = firestoreId

        )

        FeligresViewModel.editarUsuario(feligresActualizado)
    }


}
