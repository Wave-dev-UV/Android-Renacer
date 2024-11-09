package com.example.gestrenacer.utils

import android.util.Log
import java.util.Calendar

object FechasAux {
    fun calcEdadLista(anos: List<String>): List<Int> {
        val lista = anos.map { it.toInt() + 1900 }
        val anoActual = Calendar.getInstance().get(Calendar.YEAR)

        return listOf(anoActual - lista[0] - 1,anoActual - lista[1] + 1)
    }

    fun detTextoEdad(inicial: Int, final: Int): String {
        val anoActual = Calendar.getInstance().get(Calendar.YEAR)
        val edadInicial = anoActual-(1900+inicial)-1
        val edadFinal = anoActual-(1900+final)+1

        if (edadInicial >= 100 && edadFinal <=0){
            return "todas las edades."
        }
        else if (edadInicial < 100 && edadFinal <= 0){
            return "edades hasta los ${edadInicial} años."
        }
        else if (edadInicial >= 100 && edadFinal > 0){
            return "edades desde los ${edadFinal} años"
        }
        else{
            return "edades entre los ${edadFinal} y ${edadInicial} años."
        }
    }
}