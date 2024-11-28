package com.example.gestrenacer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.repository.ImagesRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val imageRepositorio: ImagesRepositorio
): ViewModel() {

    private val _uploadResult = MutableLiveData<Result<MutableMap<Any?, Any?>>>()

    fun uploadImage(file: File){
        viewModelScope.launch {
            imageRepositorio.uploadImage(file)
        }
    }
}