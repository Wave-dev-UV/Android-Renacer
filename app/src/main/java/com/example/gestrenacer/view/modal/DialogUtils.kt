package com.example.gestrenacer.view.modal

import android.content.Context
import androidx.appcompat.app.AlertDialog

class DialogUtils {
    companion object {
        fun dialogoConfirmacion(
            context: Context,
            mensaje: String,
            onYes: () -> Unit,
        ) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmación")
                .setMessage(mensaje)
                .setPositiveButton("Sí") { dialog, _ ->
                    onYes()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        fun dialogoInformativo(
            context: Context,
            titulo: String,
            mensaje: String,
            txtBtn: String
        ): AlertDialog {

            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(txtBtn) { dialog, _ ->
                    dialog.dismiss()
                }
            return builder.create()
        }
    }
}