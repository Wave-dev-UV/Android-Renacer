package com.example.gestrenacer.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Group(
    var nombre: String,
    var datesfilters: List<Timestamp>,
    var checkboxfilters: List<String>
) : Serializable {
    constructor() : this (
        "",
        emptyList(),
        emptyList()
    )

    override fun toString(): String {
        return nombre
    }
}
