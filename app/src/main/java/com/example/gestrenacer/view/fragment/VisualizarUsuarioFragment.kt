package com.example.gestrenacer.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentVisualizarUsuarioBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.Format
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisualizarUsuarioFragment : Fragment() {
    private lateinit var binding: FragmentVisualizarUsuarioBinding
    private val viewmodel: UserViewModel by viewModels()
    private lateinit var user: User
    private lateinit var rol: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisualizarUsuarioBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarVariables()
        inicializarImagen()
        formatearFechas()
        observerProgressBar()
        manejadorBotonEditar()
        manejadorBotonBorrar()
        manejadorBotonVolver()
        desactivarBtn()
    }

    private fun desactivarBtn() {
        if (rol !in listOf("Administrador", "Gestor")) {
            binding.buttonEditar.isVisible = false
        }

        if (rol != "Administrador"){
            binding.buttonBorrar.isVisible = false
        }
    }

    private fun formatearFechas() {
        binding.tvFechaNacimiento.text = user.fechaNacimiento?.let { Format.timestampToString(it) }
        binding.tvFechaRegistro.text = user.fechaCreacion?.let { Format.timestampToString(it) }
    }

    private fun observerProgressBar() {
        viewmodel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
        }
    }

    private fun inicializarImagen() {
        if (binding.imagenUsuario.drawable == null) {
            binding.imagenUsuario.isVisible = false
            binding.tvIniciales.isVisible = true
            val text =
                "${user.nombre.firstOrNull() ?: ""}${user.apellido.firstOrNull() ?: ""}".uppercase()
            binding.tvIniciales.text = text
        } else {
            binding.imagenUsuario.isVisible = true
            binding.tvIniciales.isVisible = false
        }
    }

    private fun inicializarVariables() {
        user = arguments?.getSerializable("dataFeligres") as User
        rol = activity?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador") as String

        val actividad = activity as MainActivity
        actividad.visibilidadBottomBar(false)

        binding.user = user
    }

    private fun manejadorBotonEditar() {
        binding.buttonEditar.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("dataFeligres", user)
                putString("rol", rol)
            }
            findNavController().navigate(
                R.id.action_visualizarUsuarioFragment_to_editarUsuarioFragment,
                bundle
            )
        }
    }

    private fun manejadorBotonBorrar() {
        binding.buttonBorrar.setOnClickListener {
            val preferences = activity?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val numero = preferences?.getString("numero", "")
            val numeroVacio = numero?.isNotEmpty() as Boolean

            if (numeroVacio && (numero == user.celular)) {
                DialogUtils.dialogoInformativo(
                    requireContext(),
                    getString(R.string.titModalError),
                    getString(R.string.txtErrorBorrarTodos),
                    getString(R.string.txtBtnAceptar)
                ).show()
            } else {
                DialogUtils.dialogoConfirmacion(
                    requireContext(),
                    "¿Está seguro que deseas borrar al usuario?"
                ) {
                    viewmodel.borrarUsuario(user)
                    findNavController().popBackStack()
                }
            }
        }

    }

    private fun manejadorBotonVolver() {
        binding.imageButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}