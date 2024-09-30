package com.example.gestrenacer.models

import java.io.Serializable

data class Feligres(
    var nombre: String?,
    var apellido: String?,
    var telefono: String?,
    var cedula: String?,
    var eps: String?,
    var nombreContactoEmergencia: String?,
    var telfonoContactoEmergencia: String?,
    var parentescoContactoEmergencia: String?,
    var direccion: String?,
    var rol: String?,
    var contrasena: String?
): Serializable {
    constructor(): this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null)
}
