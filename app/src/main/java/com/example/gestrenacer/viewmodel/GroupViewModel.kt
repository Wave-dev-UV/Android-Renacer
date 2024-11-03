package com.example.gestrenacer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Group
import com.example.gestrenacer.repository.GroupRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepositorio
) : ViewModel() {

    private val _listaGroups = MutableLiveData<List<Group>>()
    val listaGroups: LiveData<List<Group>> = _listaGroups

    fun getGroups() {
        viewModelScope.launch {
            try {
                // Use postValue to set LiveData safely from a background thread
                _listaGroups.postValue(repository.getGroups())
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to fetch groups: ${e.message}", e)
            }
        }
    }

    fun saveGroup(group: Group) {
        viewModelScope.launch {
            try {
                repository.saveGroup(group)
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to save group: ${e.message}", e)
            }
        }
    }
}
