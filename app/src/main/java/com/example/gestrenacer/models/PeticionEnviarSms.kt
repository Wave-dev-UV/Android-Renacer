package com.example.gestrenacer.models

data class PeticionEnviarSms(
    val telefono: String,
    val mensaje: String,
    val numeros: List<String>
)
