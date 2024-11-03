package com.example.gestrenacer.utils

import java.util.Calendar

object FechasAux {
    fun calcEdadLista(anos: List<String>): List<Int> {
        val lista = anos.map { it.toInt() + 1900 }
        val anoActual = Calendar.getInstance().get(Calendar.YEAR)

        return listOf(anoActual - lista[0] - 1,anoActual - lista[1] + 1)
    }
}