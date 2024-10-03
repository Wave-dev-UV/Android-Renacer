package com.example.gestrenacer.view.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.gestrenacer.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.sheet_filtros, container, false)
        val btnCerrar = binding.findViewById<Button>(R.id.btnCerrarFiltros)

        btnCerrar.setOnClickListener {
          dismiss()
        }

        return binding
    }

        companion object {
        const val TAG = "BottomSheetFiltros"
    }
}