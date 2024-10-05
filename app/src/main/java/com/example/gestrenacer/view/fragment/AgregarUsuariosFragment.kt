package com.example.gestrenacer.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentAgregarUsuariosBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.dialog.DialogUtils
import com.example.gestrenacer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgregarUsuariosFragment : Fragment() {
    private lateinit var binding: FragmentAgregarUsuariosBinding
    private val appViewModel: UserViewModel by viewModels()
    private val user = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgregarUsuariosBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.user = user
        binding.viewModel = appViewModel
        controler()
    }

    private fun controler() {
        activarBoton()
        confSelTipoId()
        manejadorBtnVolver()
        manejadorBtnEnviar()
    }

    private fun activarBoton(){
        val listTxt = listOf(
            binding.editTextNombre, binding.editTextApellido, binding.editTextId, binding.editTextCelular,
            binding.editTextDireccion, binding.editTextEps, binding.editTextNombreContacto,
            binding.editTextCelularContacto, binding.editTextParentescoContacto, binding.editTextDireccionContacto,
            binding.autoCompleteTipoId)

        for (i in listTxt){
            i.addTextChangedListener {
                val isFull = listTxt.all {
                    it.text.toString().isNotEmpty()
                }

                binding.buttonEnviar.isEnabled = isFull

            }
        }
    }

    private fun confSelTipoId(){
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaTipoDocumento,
            android.R.layout.simple_dropdown_item_1line
        )

        binding.autoCompleteTipoId.setAdapter(adapter)
    }

    private fun manejadorBtnVolver(){
        binding.btnVolver.setOnClickListener {
            findNavController().navigate(R.id.action_agregarUsuariosFragment_to_listarFragment,requireArguments())
        }
    }

    private fun manejadorBtnEnviar(){
        binding.buttonEnviar.setOnClickListener {
            DialogUtils.dialogoConfirmacion(requireContext(),
                "¿Está seguro que desea añadir al usuario?"){
                val user = binding.user ?: User()
                appViewModel.crearUsuario(user)
                findNavController().navigate(R.id.action_agregarUsuariosFragment_to_listarFragment,requireArguments())
            }

        }
    }
}