package com.example.gestrenacer.models

import java.io.Serializable

data class User(
    var nombre: String?,
    var apellido: String?,
    var id: String?,
    var tipoId: String?,
    var celular: String?,
    var direccion: String?,
    var eps: String?,
    var nombreContacto: String?,
    var celularContacto: String?,
    var parentescoContacto: String?,
    var direccionContacto: String?,
    var esLider: Boolean?,
    var tieneAcceso: Boolean?
): Serializable {
    constructor(): this(null, null, null, null, null, null,
        null, null, null, null, null,
        null, null)
}
