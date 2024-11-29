package com.example.gestrenacer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gestrenacer.R
import com.example.gestrenacer.models.Plantilla

class PlantillaDialogFragment(
    private val plantilla: Plantilla,
    private val onDelete: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_plantilla, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nombreTextView: TextView = view.findViewById(R.id.tvNombrePlantilla)
        val mensajeTextView: TextView = view.findViewById(R.id.tvMensajePlantilla)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)

        // Mostrar los datos de la plantilla en el diálogo
        nombreTextView.text = plantilla.name
        mensajeTextView.text = plantilla.message

        // Configurar el botón de eliminar
        btnEliminar.setOnClickListener {
            onDelete()
            dismiss() // Cerrar el diálogo
        }
    }
}
