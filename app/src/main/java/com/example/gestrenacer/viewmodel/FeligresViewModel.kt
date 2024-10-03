package com.example.gestrenacer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Feligres
import com.example.gestrenacer.repository.FeligresRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeligresViewModel @Inject constructor(private val repository: FeligresRepositorio): ViewModel() {

    fun editarUsuario(feligres: Feligres){
        viewModelScope.launch {
            repository.updateUser(feligres)
        }
    }

}