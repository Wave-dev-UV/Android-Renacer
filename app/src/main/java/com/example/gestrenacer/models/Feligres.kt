package com.example.gestrenacer.models

import java.io.Serializable

data class Feligres(
    var nombre: String,
    var apellido: String,
    var id: String, // Documento de identidad
    var firestoreId: String, // ID de Firestore  (solo para manejo interno)
    var tipoId: String,
    var celular: String,
    var direccion: String,
    var eps: String,
    var nombreContacto: String,
    var celularContacto: String,
    var parentescoContacto: String,
    var direccionContacto: String,
    var esLider: Boolean,
    var tieneAcceso: Boolean,
    var estadoAtencion: String
) : Serializable {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", false, false, "")
}
