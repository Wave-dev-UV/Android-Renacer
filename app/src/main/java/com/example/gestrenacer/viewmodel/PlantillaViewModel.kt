package com.example.gestrenacer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.models.Plantilla
import com.example.gestrenacer.repository.PlantillaRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantillaViewModel @Inject constructor(
    private val repository: PlantillaRepositorio
) : ViewModel() {

    private val _plantillas = MutableLiveData<List<Plantilla>?>()
    val plantillas: LiveData<List<Plantilla>?> = _plantillas

    private val _progresState = MutableLiveData(false)
    val progresState: LiveData<Boolean> = _progresState

    fun obtenerPlantillas() {
        viewModelScope.launch {
            _progresState.value = true
            try {
                val listaPlantillas = repository.getAllPlantillas()
                Log.d("PlantillaViewModel", "Plantillas obtenidas: $listaPlantillas")
                _plantillas.value = listaPlantillas
            } catch (e: Exception) {
                Log.e("PlantillaViewModel", "Error al obtener plantillas: ${e.message}")
            } finally {
                _progresState.value = false
            }
        }
    }

    fun crearPlantilla(plantilla: Plantilla) {
        viewModelScope.launch {
            try {
                repository.savePlantilla(plantilla)
                obtenerPlantillas()  // Actualiza la lista de plantillas
            } catch (e: Exception) {
                Log.e("PlantillaViewModel", "Error al crear plantilla: ${e.message}")
            }
        }
    }

//
//    fun editarPlantilla(plantilla: Plantilla) {
//        viewModelScope.launch {
//            try {
//                repository.updatePlantilla(plantilla)
//                obtenerPlantillas()  // Actualiza la lista de plantillas
//            } catch (e: Exception) {
//                Log.e("PlantillaViewModel", "Error al editar plantilla: ${e.message}")
//            }
//        }
//    }
//
fun eliminarPlantillas(plantillas: List<Plantilla>) {
    viewModelScope.launch {
        try {
            repository.eliminarPlantillas(plantillas)  // Llama al método correcto en el repositorio
            Log.d("PlantillaViewModel", "Plantillas eliminadas con éxito")

            // Actualiza la lista de plantillas después de la eliminación
            val listaActualizada = _plantillas.value?.filterNot { plantilla ->
                plantillas.any { it.id == plantilla.id }  // Compara correctamente los IDs
            }
            _plantillas.value = listaActualizada

        } catch (e: Exception) {
            Log.e("PlantillaViewModel", "Error al eliminar plantillas: ${e.message}")
        }
    }
}

}
