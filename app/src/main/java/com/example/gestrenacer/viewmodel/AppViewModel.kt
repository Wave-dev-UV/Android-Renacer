package com.example.gestrenacer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.FeligresRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val repository: FeligresRepositorio): ViewModel() {
    fun crearUsuario(user: User){
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }
}