package com.example.gestrenacer.cloudinary

import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.gestrenacer.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CloudinaryAccount {
    private val cloudinary: Cloudinary

    init {

        val config = ObjectUtils.asMap(
            "cloud_name", Constants.CLOUD_NAME,
            "api_key", Constants.API_KEY,
            "api_secret", Constants.API_SECRET
        )

        cloudinary = Cloudinary(config)
    }

    suspend fun uploadFile(file: File): Map<String,String>? {
        return withContext(Dispatchers.IO) {

            val uploadParams = HashMap<String, String>()
            uploadParams["folder"] = "Renacer"

            try {
                val uploadResult = cloudinary.uploader().upload(file, uploadParams)
                Log.d("Cloudinary", uploadResult.toString())

                val response = mapOf(
                    "public_id" to uploadResult["public_id"].toString(),
                    "url" to uploadResult["secure_url"].toString()
                )

                response
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun deleteFile(publicId: String){
        return withContext(Dispatchers.IO){
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}