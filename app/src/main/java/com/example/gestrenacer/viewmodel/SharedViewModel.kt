package com.example.gestrenacer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(): ViewModel() {
    private val _selectedOption = MutableLiveData("null")
    val selectedOption: LiveData<String> = _selectedOption

    fun selectGallery(){
        _selectedOption.value = "gallery"
    }

    fun selectCamera(){
        _selectedOption.value = "camera"
    }

    fun selectDelete(){
        _selectedOption.value = "delete"
    }

    fun selectNull(){
        _selectedOption.value = "null"
    }
}