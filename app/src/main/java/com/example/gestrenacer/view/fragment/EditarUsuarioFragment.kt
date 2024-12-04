package com.example.gestrenacer.view.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentEditarUsuarioBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class EditarUsuarioFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentEditarUsuarioBinding
    private lateinit var bundleUser: User
    private lateinit var rol: String
    private var fechaNacimientoUser: Timestamp? = null

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

    private fun controlador() {
        anadirRol()
        menuRol()
        observerProgress()
        observerOperacion()
        inicializarAdaptadores()
        inicializarFeligres()
        activarBoton()
        manejadorBtnVolver()
        manejadorBtnEditar()
        confSelSexo()
        confSelEstadoCivil()
        manejadorFechaNacimiento()
    }

    private fun confSelSexo() {
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaSexos,
            android.R.layout.simple_dropdown_item_1line
        )

        binding.autoCompleteSexo.setAdapter(adapter)
    }

    private fun confSelEstadoCivil() {
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaEstadoCivil,
            android.R.layout.simple_dropdown_item_1line
        )

        binding.autoCompleteEstadoCivil.setAdapter(adapter)
    }


    private fun manejadorFechaNacimiento() {
        binding.editTextFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
        }
    }

    private fun mostrarDatePicker() {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.ThemeOverlay_App_Dialog,
            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val fechaNacimiento = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }.time

                fechaNacimientoUser = Timestamp(fechaNacimiento)
                val fechatext = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                binding.editTextFechaNacimiento.setText(fechatext)
            }, year, month, day
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun anadirRol() {
        val pref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador")
        rol = pref as String
    }

    private fun observerProgress() {
        userViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.contPrincipal.isVisible = !it
        }
    }

    private fun observerOperacion() {
        userViewModel.resOperacion.observe(viewLifecycleOwner) {
            var mensaje = ""
            when (it) {
                1 -> mensaje = getString(R.string.txtModalTelRep)
                2 -> mensaje = getString(R.string.txtModalTelExcep)
            }

            if (mensaje.isNotEmpty()) {
                DialogUtils.dialogoInformativo(
                    requireContext(),
                    getString(R.string.titModalError),
                    mensaje,
                    getString(R.string.txtBtnAceptar)
                ).show()
            }

            if (it == 0) {
                findNavController().navigate(R.id.action_editarUsuarioFragment_to_listarFragment)
            }
            else binding.contPrincipal.isVisible = true
        }
    }

    private fun menuRol() {
        if (rol == "Administrador"){
            binding.contRol.isVisible = true
        }
    }

    private fun formatearFecha(timestamp: Timestamp): String {
        val date: Date = timestamp.toDate()
        val format = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return format.format(date)
    }

    private fun inicializarFeligres() {
        val receivedBundle = arguments

        if (receivedBundle != null) {
            bundleUser = receivedBundle.getSerializable("dataFeligres") as User

            fechaNacimientoUser = bundleUser.fechaNacimiento

            binding.editTextNombre.setText(bundleUser.nombre)
            binding.editTextApellido.setText(bundleUser.apellido)
            binding.editTextId.setText(bundleUser.id)
            binding.editTextCelular.setText(bundleUser.celular)
            binding.editTextDireccion.setText(bundleUser.direccion)
            binding.editTextEps.setText(bundleUser.eps)
            binding.editTextEmail.setText(bundleUser.correo)
            binding.editTextNombreContacto.setText(bundleUser.nombreContacto)
            binding.editTextCelularContacto.setText(bundleUser.celularContacto)
            binding.editTextParentescoContacto.setText(bundleUser.parentescoContacto)
            binding.editTextDireccionContacto.setText(bundleUser.direccionContacto)
            binding.autoCompleteSexo.setText(bundleUser.sexo, false)
            binding.autoCompleteEstadoCivil.setText(bundleUser.estadoCivil, false)
            binding.editTextObsevaciones.setText(bundleUser.obsevaciones)
            binding.editTextFechaNacimiento.setText(bundleUser.fechaNacimiento?.let {
                formatearFecha(
                    it
                )
            })

            // Forzar delay para asegurarse de que los adaptadores estén completamente listos
            binding.autoCompleteTipoId.post {
                binding.autoCompleteTipoId.setText(bundleUser.tipoId, false)
            }

            binding.switch1.isChecked = bundleUser.esLider
            binding.autoCompleteRole.post {
                binding.autoCompleteRole.setText(bundleUser.rol, false)
            }

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
            binding.editTextEmail,
            binding.editTextDireccion,
            binding.editTextEps,
            binding.editTextNombreContacto,
            binding.editTextCelularContacto,
            binding.editTextParentescoContacto,
            binding.editTextDireccionContacto,
            binding.autoCompleteRole
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
        if (isFull) binding.buttonEditar.setBackgroundColor(resources.getColor(R.color.azulPrincipal))
        else binding.buttonEditar.setBackgroundColor(resources.getColor(R.color.secondary))
    }

    private fun manejadorBtnVolver() {
        binding.toolbar.lblToolbar.text = getString(R.string.tituloEditarUsuario)
        binding.toolbar.btnVolver.setOnClickListener {
            hideKeyboard()
            findNavController().popBackStack()
        }
    }


    private fun inicializarAdaptadores() {

        val adapterDocumento = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaTipoDocumento,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteTipoId.setAdapter(adapterDocumento)

        val adapterEdit = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaRoles,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteRole.setAdapter(adapterEdit)

        val adapterAtencion = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaEstadoAtencion,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteEstadoAtencion.setAdapter(adapterAtencion)

    }


    private fun manejadorBtnEditar() {
        binding.buttonEditar.setOnClickListener {
            hideKeyboard()
            DialogUtils.dialogoConfirmacion(
                requireContext(),
                "¿Está seguro que desea editar el usuario?"
            ) {
                updateFeligres()
            }
        }
    }

    private fun updateFeligres() {
        val feligresActualizado = User(
            binding.editTextNombre.text.toString(),
            binding.editTextApellido.text.toString(),
            binding.editTextId.text.toString(),
            binding.autoCompleteTipoId.text.toString(),
            binding.editTextCelular.text.toString(),
            binding.editTextDireccion.text.toString(),
            binding.editTextEps.text.toString(),
            binding.editTextNombreContacto.text.toString(),
            binding.editTextCelularContacto.text.toString(),
            binding.editTextParentescoContacto.text.toString(),
            binding.editTextDireccionContacto.text.toString(),
            binding.autoCompleteEstadoAtencion.text.toString(),
            firestoreId,
            binding.switch1.isChecked,
            binding.autoCompleteRole.text.toString(),
            binding.autoCompleteSexo.text.toString(),
            binding.autoCompleteEstadoCivil.text.toString(),
            fechaNacimientoUser,
            binding.editTextObsevaciones.text.toString(),
            bundleUser.fechaCreacion,
            bundleUser.imageId,
            bundleUser.imageUrl,
            binding.editTextEmail.text.toString()
        )

        userViewModel.editarUsuario(feligresActualizado, bundleUser.correo)
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun Fragment.hideKeyboard() {
        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.hideKeyboard()
        }
    }
}
