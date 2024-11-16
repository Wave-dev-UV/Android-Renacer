package com.example.gestrenacer.utils


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
}