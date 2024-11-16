package com.example.gestrenacer.webservices

import com.example.gestrenacer.models.PeticionEnviarSms
import com.example.gestrenacer.models.SmsApiRes
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SmsService {
    @POST("/sms")
    suspend fun enviarSms(
        @Body body: PeticionEnviarSms,
        @Header("X-Firebase-AppCheck") auth: String
    ): SmsApiRes
}