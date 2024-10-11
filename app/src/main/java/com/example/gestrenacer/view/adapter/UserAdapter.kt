package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ItemUserBinding
import com.example.gestrenacer.models.User

class UserAdapter(
    private val listaUsers: List<User>,
    private val navController: NavController,
    private val rol: String?
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol)
    }

    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val feligres = listaUsers[position]
        holder.setItemUser(feligres)
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?
    ): RecyclerView.ViewHolder(binding.root) {

        fun setItemUser(user: User){
            val nombre = user.nombre
            val apellido = user.apellido

            binding.lblIniciales.text = "${nombre.get(0)}${apellido.get(0)}".uppercase()
            binding.txtNombre.text = "${nombre} ${apellido}."
            binding.txtCelular.text = "${user.celular}."
            binding.txtRol.text = "${user.rol}."
            binding.txtEsLider.text = (
                    if (user.esLider) "Si."
                    else "No.")

            manejadorClicCard(user)
        }

        private fun manejadorClicCard(user: User){
            if (rol != "Visualizador"){
                binding.cardFeligres.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putSerializable("dataFeligres",user)
                    bundle.putString("rol",rol)
                    navController.navigate(R.id.action_listarFragment_to_editarUsuarioFragment, bundle)
                }
            }
        }
    }
}