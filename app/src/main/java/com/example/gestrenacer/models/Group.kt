package com.example.gestrenacer.models

import java.io.Serializable

data class Group(
    var nombre: String,
    var keyvalfilters: List<Pair<String, String>>,
    var listfilters: List<String>
) : Serializable {
    constructor() : this (
        "",
        emptyList(),
        emptyList()
    )
}
