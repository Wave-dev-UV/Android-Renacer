package com.example.gestrenacer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.databinding.ItemPlantillaBinding
import com.example.gestrenacer.models.Plantilla

class PlantillaAdapter(
    private val plantillas: List<Plantilla>,
    private val onPlantillaClick: (Plantilla) -> Unit // Callback para manejar el clic
) : RecyclerView.Adapter<PlantillaAdapter.PlantillaViewHolder>() {

    // ViewHolder para representar cada item en el RecyclerView
    inner class PlantillaViewHolder(private val binding: ItemPlantillaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plantilla: Plantilla) {
            binding.tvNombrePlantilla.text = plantilla.name
            binding.tvMensajePlantilla.text = plantilla.message

            // Configurar el clic
            binding.root.setOnClickListener {
                onPlantillaClick(plantilla) // Llamar al callback
            }
        }
    }

    // Crear un nuevo ViewHolder cuando no haya suficientes ViewHolders disponibles
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantillaViewHolder {
        val binding = ItemPlantillaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantillaViewHolder(binding)
    }

    // Enlazar los datos de cada plantilla a su correspondiente ViewHolder
    override fun onBindViewHolder(holder: PlantillaViewHolder, position: Int) {
        val plantilla = plantillas[position]
        holder.bind(plantilla)
    }

    // Número total de elementos en la lista
    override fun getItemCount(): Int {
        return plantillas.size
    }
}
