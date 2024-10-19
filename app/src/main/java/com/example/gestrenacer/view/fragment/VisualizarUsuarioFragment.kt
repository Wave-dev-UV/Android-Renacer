package com.example.gestrenacer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentVisualizarUsuarioBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.Format
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
    }

    private fun formatearFechas(){
        binding.tvFechaNacimiento.text = user.fechaNacimiento?.let { Format.timestampToString(it) }
        binding.tvFechaRegistro.text = user.fechaCreacion?.let { Format.timestampToString(it) }
    }

    private fun observerProgressBar(){
        viewmodel.progresState.observe(viewLifecycleOwner){
            binding.progress.isVisible = it
        }
    }

    private fun inicializarImagen(){
        if (binding.imagenUsuario.drawable == null){
            binding.imagenUsuario.isVisible = false
            binding.tvIniciales.isVisible = true
            val text = "${user.nombre.firstOrNull() ?: ""}${user.apellido.firstOrNull() ?: ""}".uppercase()
            binding.tvIniciales.text = text
        } else {
            binding.imagenUsuario.isVisible = true
            binding.tvIniciales.isVisible = false
        }
    }

    private fun inicializarVariables(){
        user = arguments?.getSerializable("dataFeligres") as User
        rol = arguments?.getString("rol") ?: "Visualizador"
        binding.user = user
    }

    private fun manejadorBotonEditar(){
        binding.buttonEditar.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("dataFeligres", user)
                putString("rol", rol)
            }
            findNavController().navigate(R.id.action_visualizarUsuarioFragment_to_editarUsuarioFragment, bundle)
        }
    }

    private fun manejadorBotonBorrar() {
        binding.buttonBorrar.setOnClickListener {
            DialogUtils.dialogoConfirmacion(requireContext(),
                "¿Está seguro que deseas borrar al usuario?"){
                viewmodel.borrarUsuario(user)
                findNavController().navigate(R.id.action_visualizarUsuarioFragment_to_listarFragment, requireArguments())
            }
        }

    }

    private fun manejadorBotonVolver(){
        binding.imageButton.setOnClickListener{
            findNavController().navigate(R.id.action_visualizarUsuarioFragment_to_listarFragment, requireArguments())
        }
    }


}