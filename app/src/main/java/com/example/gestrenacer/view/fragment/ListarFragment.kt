package com.example.gestrenacer.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentListarFeligresesBinding
import com.example.gestrenacer.view.adapter.FeligresAdapter
import com.example.gestrenacer.view.modal.ModalBottomSheet
import com.example.gestrenacer.viewmodel.FeligresViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListarFragment : Fragment() {
    private lateinit var binding: FragmentListarFeligresesBinding
    private val feligresViewModel: FeligresViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListarFeligresesBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //feligresViewModel.getFeligreses()
        //iniciarComponentes()
    }

    private fun iniciarComponentes(){
        //observerListFeligreses()
        //observerProgress()
        /*manejadorBtnAnadir()
        manejadorBtnMensaje()*/
        //manejadorBtnFiltro()
    }

//    private fun observerListFeligreses(){
//        feligresViewModel.listaFeligreses.observe(viewLifecycleOwner){
//            val recyclerView = binding.listaFeligreses
//            recyclerView.layoutManager = LinearLayoutManager(context)
//            val adapter = FeligresAdapter(it, findNavController())
//            recyclerView.adapter = adapter
//
//            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    activity?.finish()
//                }
//            })
//        }
//    }

    /*private fun observerProgress(){
        feligresViewModel.progresState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
        }
    }*/

    /*private fun manejadorBtnFiltro() {
        binding.btnFiltrar.setOnClickListener{
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(requireActivity().supportFragmentManager,ModalBottomSheet.TAG)
        }
    }*/

    /*private fun manejadorBtnMensaje() {
        binding.btnEnviarSms.setOnClickListener{
            Log.d("BtnSMS","Clic en el botón de SMS")
        }
    }

    private fun manejadorBtnAnadir(){
        binding.btnAnadirFeligres.setOnClickListener{
            Log.d("BtnAnadir","Clic en el botón de añadir")
            //findNavController()
        }
    }*/
}