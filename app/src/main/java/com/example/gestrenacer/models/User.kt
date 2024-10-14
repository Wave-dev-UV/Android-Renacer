package com.example.gestrenacer.models

import java.io.Serializable

data class User(
    var nombre: String,
    var apellido: String,
    var id: String,
    var tipoId: String,
    var celular: String,
    var direccion: String,
    var eps: String,
    var nombreContacto: String,
    var celularContacto: String,
    var parentescoContacto: String,
    var direccionContacto: String,
    var estadoAtencion: String,
    var firestoreID: String,
    var esLider: Boolean,
    var rol: String,
    var sexo: String,
    var estadoCivil: String,
    var fechaNacimiento: String,
    var obsevaciones: String
): Serializable {
    constructor(): this("", "", "", "", "", "",
        "", "", "", "", "",
        "","",false, "Feligrés", "", "", "", "")
}