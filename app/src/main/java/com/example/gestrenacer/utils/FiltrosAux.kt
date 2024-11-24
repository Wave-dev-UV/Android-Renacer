package com.example.gestrenacer.utils

import com.example.gestrenacer.models.User


object FiltrosAux {
    fun clasificarFiltros(
        lista: List<String>,
        listaCivil: List<String>,
        listaSexo: List<String>
    ): List<List<String>> {
        val sexo: MutableList<String> = mutableListOf()
        val estado: MutableList<String> = mutableListOf()

        for (i in lista) {
            when (i) {
                in listaCivil -> estado.add(i)
                in listaSexo -> sexo.add(i)
            }
        }

        return listOf(sexo, estado)
    }

    fun ordenar(lista: MutableList<User>, criterio: String, escala: String): MutableList<User> {
        when (criterio) {
            "nombre" -> {
                if (escala == "ascendente") return lista.sortedBy { it.nombre.lowercase() }.toMutableList()
                else return lista.sortedByDescending { it.nombre.lowercase() }.toMutableList()
            }

            "fechaNacimiento" -> {
                if (escala == "ascendente") return lista.sortedByDescending { it.fechaNacimiento }
                    .toMutableList()
                else return lista.sortedBy { it.fechaNacimiento }.toMutableList()
            }

            "fechaCreacion" -> {
                if (escala == "ascendente") return lista.sortedByDescending { it.fechaCreacion }
                    .toMutableList()
                else return lista.sortedBy { it.fechaCreacion }.toMutableList()
            }
            else -> {
                return lista
            }
        }
    }
}