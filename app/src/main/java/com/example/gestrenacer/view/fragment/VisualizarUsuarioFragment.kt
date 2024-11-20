package com.example.gestrenacer.view.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide

@AndroidEntryPoint
class VisualizarUsuarioFragment : Fragment() {
    private lateinit var binding: FragmentVisualizarUsuarioBinding
    private val viewmodel: UserViewModel by viewModels()
    private val sharedViewmodel: SharedViewModel by activityViewModels()
    private lateinit var user: User
    private lateinit var rol: String
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoFile: File


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

        /*
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ){activityForTakePicture(it)}
         */
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
    }

    private fun inicializarUrlImagen(){
        val img = (if (user.imageUrl?.isEmpty() as Boolean) R.drawable.defecto
        else user.imageUrl)

        Glide.with(binding.root.context)
            .load(img)
            .into(binding.imagenUsuario)
    }

    private fun observeImageChange(){
        viewmodel.listaUsers.observe(viewLifecycleOwner){list ->
            val myUser = list?.find { it.firestoreID == user.firestoreID }
            Log.d("IMPORTANTE", "CAMBIE A ${myUser?.imageUrl}")
            Glide.with(requireContext())
                .load(myUser?.imageUrl)
                .into(binding.imagenUsuario)
            if (myUser != null) {
                user = myUser
            }
        }
    }

    private fun uploadImage(uri: Uri?){
        if (uri == null) {
            return Toast.makeText(
                requireContext(),
                "No se selecciono ninguna imagen",
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

    private fun activityForTakePicture(isSuccess: Boolean){
        if (isSuccess){
            Log.d("IMPORTANTE", "Andamos subiendo la imagen")
            viewmodel.uploadImage(photoFile, user)
        } else {
            Toast.makeText(
                requireContext(),
                "No se tomo ninguna foto",
                Toast.LENGTH_SHORT
            ).show()
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
            }
            /*else if (option == "camera"){
                try {
                    photoFile = createImageFile()
                    val photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireActivity().packageName}.fileprovider",
                        photoFile)
                    takePictureLauncher.launch(photoUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } */ else if (option == "delete"){
                DialogUtils.dialogoConfirmacion(
                    requireContext(),
                    "¿Deseas eliminar la imagen de perfil?"
                ){
                    user.imageId?.let { viewmodel.deleteImage(it, user) }
                }
            }

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun manejadorBotonEditarImagen() {
        binding.btnEditarImagen.setOnClickListener {
            val bottomSheet = BottomSheetModalFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }
    }


    private fun desactivarBtn(){
        if (rol !in listOf("Administrador","Gestor")){
            binding.buttonBorrar.isVisible = false
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
            binding.toolbarVerUsuario.isVisible = !it
            binding.buttonEditar.isVisible = !it
            binding.buttonBorrar.isVisible = !it
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
        binding.toolbar.lblToolbar.text = getString(R.string.ver_usuario)
        binding.toolbar.btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}