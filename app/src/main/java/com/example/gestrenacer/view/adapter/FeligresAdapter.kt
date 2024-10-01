package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.databinding.ItemFeligresBinding
import com.example.gestrenacer.models.Feligres

class FeligresAdapter(
    private val listaFeligreses: List<Feligres>,
    private val navController: NavController
): RecyclerView.Adapter<FeligresAdapter.FeligresViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeligresViewHolder {
        val binding = ItemFeligresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeligresViewHolder(binding, navController)
    }

    override fun getItemCount(): Int {
        return listaFeligreses.size
    }

    override fun onBindViewHolder(holder: FeligresViewHolder, position: Int) {
        val feligres = listaFeligreses[position]
        holder.setCardFeligres(feligres)
    }

    class FeligresViewHolder(
        private val binding: ItemFeligresBinding,
        private val navController: NavController
    ): RecyclerView.ViewHolder(binding.root) {

        fun setCardFeligres(feligres: Feligres){
            val nombre = feligres.nombre
            val apellido = feligres.apellido

            binding.lblIniciales.text = "${nombre?.get(0)}${apellido?.get(0)}".uppercase()
            binding.txtNombre.text = "${nombre} ${apellido}."
            binding.txtCelular.text = "${feligres.telefono}."
            binding.txtRol.text = "${feligres.rol}."
            binding.txtEsLider.text = (
                    if (feligres.rol.toBoolean()) "Si."
                    else "No")

            binding.cardFeligres.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("feligres",feligres)
                //navController.navigate(R.id.action)
            }
        }
    }
}