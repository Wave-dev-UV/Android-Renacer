package com.example.gestrenacer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
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
    var fechaNacimiento: Timestamp?,
    var obsevaciones: String,
    var fechaCreacion: Timestamp?,
    var imageId: String?,
    var imageUrl: String?,
    var correo: String
): Serializable, Parcelable{
    constructor(): this("", "", "", "", "", "",
        "", "", "", "", "",
        "","",false, "Feligrés", "", "",
        null, "", null, "",
        "", "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(nombre)
        dest.writeString(apellido)
        dest.writeString(rol)
    }
}