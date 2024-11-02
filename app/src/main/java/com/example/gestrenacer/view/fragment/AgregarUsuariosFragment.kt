package com.example.gestrenacer.view.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentAgregarUsuariosBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar


@AndroidEntryPoint
class AgregarUsuariosFragment : Fragment() {
    private lateinit var binding: FragmentAgregarUsuariosBinding
    private lateinit var rol: String
    private val userViewModel: UserViewModel by viewModels()
    private val user = User()
    private var fechaNacimientoUser: Timestamp? = null

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
        binding.viewModel = userViewModel
        binding.user = user
        controler()
    }

    private fun controler() {
        anadirRol()
        menuRol()
        observerProgress()
        observerOperacion()
        activarBoton()
        confSelTipoId()
        confSelRol()
        manejadorBtnVolver()
        manejadorBtnEnviar()
        confSelSexo()
        confSelEstadoCivil()
        manejadorFechaNacimiento()
    }

    private fun manejadorFechaNacimiento(){
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
        val pref = activity?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador")
        val actividad = activity as MainActivity
        actividad.visibilidadBottomBar(false)
        rol = pref as String
    }
    private fun menuRol(){
        if (rol == "Administrador"){
            binding.selectRole.visibility = View.VISIBLE
            binding.txtRol.visibility = View.VISIBLE
        }
    }

    private fun observerProgress(){
        userViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.contPrincipal.isVisible = !it
        }
    }

    private fun observerOperacion(){
        userViewModel.resOperacion.observe(viewLifecycleOwner) {
            var mensaje = ""
            when (it) {
                1 -> mensaje = getString(R.string.txtModalTelRep)
                2 -> mensaje = getString(R.string.txtModalTelExcep)
            }

            if (mensaje.isNotEmpty()){
                DialogUtils.dialogoInformativo(
                    requireContext(),
                    getString(R.string.titModalError),
                    mensaje,
                    getString(R.string.txtBtnAceptar)
                ).show()
            }

            if (it == 0) findNavController().navigate(R.id.action_agregarUsuariosFragment_to_listarFragment)
            else binding.contPrincipal.isVisible = true
        }
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

    private fun confSelSexo(){
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaSexos,
            android.R.layout.simple_dropdown_item_1line
        )

        binding.autoCompleteSexo.setAdapter(adapter)
    }

    private fun confSelEstadoCivil(){
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaEstadoCivil,
            android.R.layout.simple_dropdown_item_1line
        )

        binding.autoCompleteEstadoCivil.setAdapter(adapter)
    }

    private fun confSelRol(){
        val adapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.listaRoles,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.autoCompleteRole.setAdapter(adapter)

        binding.autoCompleteRole.post {
            binding.autoCompleteRole.setText("Feligrés", false)
        }
    }

    private fun manejadorBtnVolver(){
        binding.btnVolver.setOnClickListener {
            findNavController().navigate(R.id.action_agregarUsuariosFragment_to_listarFragment)
        }
    }

    private fun manejadorBtnEnviar(){
        binding.buttonEnviar.setOnClickListener {
            DialogUtils.dialogoConfirmacion(requireContext(),
                "¿Está seguro que desea añadir al usuario?"){
                val user = binding.user ?: User()
                val newUser = user.copy(fechaNacimiento = fechaNacimientoUser)
                newUser.rol = binding.autoCompleteRole.text.toString()
                newUser.estadoAtencion = "Por Llamar"
                userViewModel.crearUsuario(newUser)
            }

        }
    }
}