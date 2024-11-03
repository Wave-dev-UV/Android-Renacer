package com.example.gestrenacer.models

import java.io.Serializable

data class Group(
    var nombre: String,
    var datesfilters: List<String>,
    var checkboxfilters: List<String>
) : Serializable {
    constructor() : this (
        "",
        emptyList(),
        emptyList()
    )
}
