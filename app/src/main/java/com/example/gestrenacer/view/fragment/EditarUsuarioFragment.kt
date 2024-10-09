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
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class EditarUsuarioFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentEditarUsuarioBinding
    private  lateinit var bundleUser: User

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


    private lateinit var firestoreId: String

    private fun controlador(){
        inicializarAdaptadores()
        inicializarFeligres()
        activarBoton()
        manejadorBtnVolver()
        manejadorBtnEditar()
    }

    private fun inicializarFeligres() {
        val receivedBundle = arguments

        if (receivedBundle != null) {
            bundleUser = receivedBundle.getSerializable("dataFeligres") as User


            binding.editTextNombre.setText(bundleUser.nombre)
            binding.editTextApellido.setText(bundleUser.apellido)
            binding.editTextId.setText(bundleUser.id)
            binding.editTextCelular.setText(bundleUser.celular)
            binding.editTextDireccion.setText(bundleUser.direccion)
            binding.editTextEps.setText(bundleUser.eps)
            binding.editTextNombreContacto.setText(bundleUser.nombreContacto)
            binding.editTextCelularContacto.setText(bundleUser.celularContacto)
            binding.editTextParentescoContacto.setText(bundleUser.parentescoContacto)
            binding.editTextDireccionContacto.setText(bundleUser.direccionContacto)

            // Forzar delay para asegurarse de que los adaptadores estén completamente listos
            binding.autoCompleteTipoId.post {
                binding.autoCompleteTipoId.setText(bundleUser.tipoId, false)
            }

            binding.switch1.isChecked = bundleUser.esLider
            binding.switch2.isChecked = bundleUser.tieneAcceso

            binding.autoCompleteEstadoAtencion.post {
                binding.autoCompleteEstadoAtencion.setText(bundleUser.estadoAtencion, false)
            }

            firestoreId = bundleUser.firestoreID
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


    private fun inicializarAdaptadores(){

        val adapterDocumento = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaTipoDocumento,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteTipoId.setAdapter(adapterDocumento)



        val adapterAtencion = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaEstadoAtencion,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteEstadoAtencion.setAdapter(adapterAtencion)

    }



    private fun manejadorBtnEditar() {
        binding.buttonEditar.setOnClickListener {
            DialogUtils.dialogoConfirmacion(requireContext(),
            "¿Está seguro que desea editar el usuario?") {
                updateFeligres()
            }
        }
    }

    private fun updateFeligres() {
        val feligresActualizado = User(
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
            firestoreID = firestoreId

        )

        userViewModel.editarUsuario(feligresActualizado)
    }


}
