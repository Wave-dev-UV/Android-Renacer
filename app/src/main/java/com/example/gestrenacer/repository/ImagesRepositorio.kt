package com.example.gestrenacer.repository

import com.example.gestrenacer.cloudinary.CloudinaryAccount
import java.io.File
import javax.inject.Inject

class ImagesRepositorio @Inject constructor() {
    private val mycloud = CloudinaryAccount()

    suspend fun uploadImage(file: File): Map<String,String>?  {
        return mycloud.uploadFile(file)
    }

    suspend fun deleteFile(publicId: String){
        mycloud.deleteFile(publicId)
    }
}