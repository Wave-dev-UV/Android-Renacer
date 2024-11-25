package com.example.gestrenacer.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Sms(
    val filtros: List<String>,
    val mensaje: String,
    val enviado: String,
    val fecha: Timestamp,
    val grupo: String
) : Serializable {
    constructor() : this(
        listOf(),
        "",
        "",
        Timestamp(2024, 1),
        ""
    )
}
