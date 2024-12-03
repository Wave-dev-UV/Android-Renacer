package com.example.gestrenacer.view.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentVisualizarUsuarioBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.utils.Format
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.SharedViewModel
import com.example.gestrenacer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import com.bumptech.glide.Glide

@AndroidEntryPoint
class VisualizarUsuarioFragment : Fragment() {
    private lateinit var binding: FragmentVisualizarUsuarioBinding
    private val viewmodel: UserViewModel by viewModels()
    private val sharedViewmodel: SharedViewModel by activityViewModels()
    private lateinit var user: User
    private lateinit var rol: String
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>


    override fun onDestroy() {
        super.onDestroy()
        sharedViewmodel.selectNull()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisualizarUsuarioBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {uploadImage(it)}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarVariables()
        inicializarUrlImagen()
        formatearFechas()
        observeImageChange()
        observeOptions()
        observerProgressBar()
        manejadorBotonEditar()
        manejadorBotonBorrar()
        manejadorBotonVolver()
        manejadorBotonEditarImagen()
        desactivarBtn()
        desactivarEditarImagenPorRol()
    }

    private fun desactivarEditarImagenPorRol() {
        if (rol !in listOf("Administrador","Gestor")){
            binding.btnEditarImagen.isVisible = false
        }
    }

    private fun inicializarUrlImagen(){
        val img = (if (user.imageUrl?.isEmpty() as Boolean) R.drawable.defecto
        else user.imageUrl)

        Glide.with(binding.root.context)
            .load(img)
            .into(binding.imagenUsuario)
    }

    private fun observeImageChange(){
        viewmodel.imageUrl.observe(viewLifecycleOwner){url ->
            user.imageUrl = url[0]
            user.imageId = url[1]

            val img = (if (user.imageUrl?.isEmpty() as Boolean) R.drawable.defecto
                    else user.imageUrl)

            Glide.with(requireContext())
                .load(img)
                .into(binding.imagenUsuario)
        }
    }

    private fun uploadImage(uri: Uri?){
        if (uri == null) {
            return Toast.makeText(
                requireContext(),
                "No se seleccionó ninguna imagen",
                Toast.LENGTH_SHORT).show()
        }
        DialogUtils.dialogoConfirmacion(
            requireContext(),
            "¿Deseas cambiar la imagen de perfil?"
        ){
            val path = getRealPathFromURI(uri)
            val file = File(path.toString())
            viewmodel.uploadImage(file, user)
        }

    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun observeOptions(){
        sharedViewmodel.selectedOption.observe(viewLifecycleOwner){option ->
            if(option == "gallery"){
                pickImageLauncher
                    .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else if (option == "delete"){
                DialogUtils.dialogoConfirmacion(
                    requireContext(),
                    "¿Deseas eliminar la imagen de perfil?"
                ){
                    user.imageId?.let { viewmodel.deleteImage(it, user) }
                }
            }

        }
    }


    private fun manejadorBotonEditarImagen() {
        binding.btnEditarImagen.setOnClickListener {
            val bottomSheet = BottomSheetModalFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun desactivarBtn(){
        if (rol !in listOf("Administrador","Gestor")){
            binding.buttonEditar.isVisible = false
        }
        if (rol != "Administrador"){
            binding.buttonBorrar.isVisible = false
        }
    }

    private fun formatearFechas() {
        if (user.fechaNacimiento != null){
            val aux = user.fechaNacimiento?.toDate()
            val expr = (aux?.year == 1) && (aux.month == 0) && (aux.date == 1)

            if (expr) {
                binding.tvFechaNacimiento.text = getString(R.string.no_especificado)
            } else {
                binding.tvFechaNacimiento.text = user.fechaNacimiento?.let { Format.timestampToString(it) }

            }

        }

        binding.tvFechaRegistro.text = user.fechaCreacion?.let { Format.timestampToString(it) }
    }

    private fun observerProgressBar() {
        viewmodel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.toolbarVerUsuario.isVisible = !it
            binding.buttonEditar.isVisible = !it
            binding.buttonBorrar.isVisible = !it

            desactivarBtn()
        }
    }


    private fun inicializarVariables() {
        user = arguments?.getSerializable("dataFeligres") as User
        rol = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
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
            val preferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
            val numero = preferences?.getString("correo", "")
            val numeroVacio = numero?.isNotEmpty() as Boolean

            if (numeroVacio && (numero == user.correo)) {
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
        binding.toolbar.lblToolbar.text = getString(R.string.ver_usuario)
        binding.toolbar.btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}