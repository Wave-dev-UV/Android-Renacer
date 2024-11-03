package com.example.gestrenacer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestrenacer.databinding.FragmentSmsBinding
import com.example.gestrenacer.view.MainActivity
import com.example.gestrenacer.view.modal.DialogUtils
import com.example.gestrenacer.viewmodel.SmsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsFragment : Fragment() {

    private lateinit var binding: FragmentSmsBinding
    private val smsViewModel: SmsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciarComponentes()
    }

    private fun iniciarComponentes() {
        manejadorTxtSms()
        manejadorBtnEnviar()
        manejadorBtnVolver()
        manejadorSwitchGuard()
        iniciarToolbar()
        iniciarFiltros()
        iniciarCantSms()
        iniciarUsuarios()
        observerProgress()
        observerOperacion()
        observerGuardado()
        observerGrupoActivado()
    }

    private fun iniciarToolbar(){
        val activity = requireActivity() as MainActivity
        activity.visibilidadBottomBar(false)
        binding.toolbar.lblToolbar.text = getString(R.string.lblMensajeria)
    }

    private fun iniciarFiltros() {
        val filtros = requireArguments().getStringArrayList("filtros") as ArrayList<String>
        Log.d("filtros sms", filtros.toString())

        for (i in filtros) {
            mostrarChip(i)
        }
    }

    private fun iniciarUsuarios() {
        val lista = requireArguments().getStringArrayList("usuarios")?.toMutableList()

        Log.d("iniciar usuario",lista.toString())
        smsViewModel.iniciarUsuarios(lista as MutableList<String>)
    }

    private fun iniciarCantSms() {
        val cant = requireArguments().getStringArrayList("usuarios")?.size

        binding.lblPersonas.text = getString(R.string.txtEnvPersonSel) + " ${cant} personas."
    }

    private fun observerGrupoActivado() {
        smsViewModel.grupoActivado.observe(viewLifecycleOwner) {
            if (it) {
                binding.filtrosCont.isVisible = false
                binding.lblFiltros.isVisible = false
                binding.lblPersonas.isVisible = false
            }
        }
    }

    private fun observerGuardado() {
        smsViewModel.guardado.observe(viewLifecycleOwner) {
            if (it) binding.btnEnviar.text = getString(R.string.lblBtnEnvGuardSms)
            else binding.btnEnviar.text = getString(R.string.lblBtnEnviarSms)

            validarTexto()
        }
    }

    private fun observerProgress() {
        smsViewModel.progress.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.toolbar.root.isVisible = !it
            binding.btnEnviar.isVisible = !it
            binding.txtPlantilla.isVisible = !it
            binding.switchGuardSms.isVisible = !it
            binding.lblPlantillaGuard.isVisible = !it
            binding.lblGuardSms.isVisible = !it
            binding.contPrincipal.isVisible = !it
        }
    }

    private fun observerOperacion() {
        smsViewModel.operacion.observe(viewLifecycleOwner) {
            when (it) {
                2 -> {
                    DialogUtils.dialogoInformativo(
                        requireContext(),
                        getString(R.string.titModalError),
                        getString(R.string.txtErrorSms),
                        getString(R.string.txtBtnAceptar)
                    ).show()
                }

                1 -> {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun mostrarChip(filtro: String) {
        val aux = filtro.split(" ")
        when (aux[0]) {
            "Casado(a)" -> binding.chipCasado.isVisible = true
            "Viudo(a)" -> binding.chipViudo.isVisible = true
            "Masculino" -> binding.chipMasculino.isVisible = true
            "Femenino" -> binding.chipFemenino.isVisible = true
            "Soltero(a)" -> binding.chipSoltero.isVisible = true
            "Unión libre" -> binding.chipLibre.isVisible = true
            "Menor" -> {
                binding.chipEdadMin.setText("Mayores de ${aux[1]} años.")
                binding.chipEdadMin.isVisible = true
            }

            "Mayor" -> {
                binding.chipEdadMax.setText("Menores de ${aux[1]} años.")
                binding.chipEdadMax.isVisible = true
            }

            "Todas" -> binding.chipTodaEdad.isVisible = true
        }
    }

    private fun manejadorTxtSms() {
        val list = listOf(binding.txtSms,binding.txtPlantilla)

        list.forEach { i ->
            i.addTextChangedListener {
                validarTexto()
            }
        }
    }

    private fun validarTexto(){
        val cant = smsViewModel.usuarios.value?.size as Int
        val grupos = smsViewModel.grupoActivado.value as Boolean
        val plantilla = smsViewModel.guardado.value as Boolean
        val nomPlantilla = binding.txtPlantilla.text.toString().isNotEmpty()
        val txtSms = binding.txtSms.text.toString().isNotEmpty()

        binding.btnEnviar.isEnabled = (txtSms && (cant > 0 || grupos) && ((plantilla && nomPlantilla) || !plantilla))

        cambiarColorBtnEnviar()
    }

    private fun manejadorBtnVolver() {
        binding.toolbar.btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun cambiarColorBtnEnviar() {
        if (binding.btnEnviar.isEnabled) {
            binding.btnEnviar.setBackgroundColor(resources.getColor(R.color.onSelectedColorBotBar))
        } else {
            binding.btnEnviar.setTextColor(resources.getColor(R.color.btnDesactivado))
        }
    }

    private fun manejadorBtnEnviar() {
        binding.btnEnviar.setOnClickListener {
            smsViewModel.enviarSms(binding.txtSms.text.toString())
        }
    }

    private fun manejadorSwitchGuard() {
        binding.switchGuardSms.setOnCheckedChangeListener { _, it ->
            binding.lblPlantilla.isEnabled = it
            smsViewModel.cambiarGuardado(it)
        }
    }
}
