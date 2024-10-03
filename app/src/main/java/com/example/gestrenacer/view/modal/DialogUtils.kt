package com.example.gestrenacer.view.modal

import android.content.Context
import androidx.appcompat.app.AlertDialog

class DialogUtils {
    companion object{
        fun dialogoConfirmacion(
            context: Context,
            onYes: ()-> Unit,
        ){
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmación")
                .setMessage("¿Estás seguro que deseas crear el usuario?")
                .setPositiveButton("Sí"){dialog, _ ->
                    onYes()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}