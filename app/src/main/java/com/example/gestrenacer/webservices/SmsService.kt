package com.example.gestrenacer.webservices

import com.example.gestrenacer.models.PeticionDesuscribir
import com.example.gestrenacer.models.PeticionEnviarSms
import com.example.gestrenacer.models.PeticionSuscribir
import com.example.gestrenacer.models.SmsApiRes
import com.example.gestrenacer.models.SmsSubsRes
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SmsService {
    @POST("/subscribe")
    suspend fun suscribirSms(
        @Body body: PeticionSuscribir,
        @Header("X-Firebase-AppCheck") auth: String
    ): SmsSubsRes

    @POST("/unsubscribe")
    suspend fun desuscribirSms(
        @Body body: PeticionDesuscribir,
        @Header("X-Firebase-AppCheck") auth: String
    ): SmsApiRes

    @POST("/sms")
    suspend fun enviarSms(
        @Body body: PeticionEnviarSms,
        @Header("X-Firebase-AppCheck") auth: String
    ): SmsApiRes
}