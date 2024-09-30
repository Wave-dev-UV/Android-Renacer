package com.example.gestrenacer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Feligres
import com.example.gestrenacer.repository.FeligresRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeligresViewModel @Inject constructor(
    private val feligresRepositorio: FeligresRepositorio
): ViewModel(){
    private val _listaFeligreses = MutableLiveData<List<Feligres>>()
    val listaFeligreses: LiveData<List<Feligres>> = _listaFeligreses

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    fun getFeligreses(){
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listaFeligreses.value = feligresRepositorio.getFeligreses()
                _progresState.value = false
            } catch (e: Exception) {
                Log.d("Error carga feligreses", e.toString())
                _progresState.value = false
            }
        }
    }
}