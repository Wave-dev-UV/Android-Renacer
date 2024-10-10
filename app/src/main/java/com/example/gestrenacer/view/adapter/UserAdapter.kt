package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.databinding.ItemUserBinding
import com.example.gestrenacer.models.User

class UserAdapter(
    private val listaUsers: List<User>,
    private val navController: NavController
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController)
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
        private val navController: NavController
    ): RecyclerView.ViewHolder(binding.root) {

        fun setItemUser(user: User){
            val nombre = user.nombre
            val apellido = user.apellido

            binding.lblIniciales.text = "${nombre.get(0)}${apellido.get(0)}".uppercase()
            binding.txtNombre.text = "${nombre} ${apellido}."
            binding.txtCelular.text = "${user.celular}."
            //binding.txtRol.text = "${feligres.rol}."
            binding.txtEsLider.text = (
                    if (user.esLider) "Si."
                    else "No.")

            binding.cardFeligres.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("feligres",user)
                //navController.navigate(R.id.action)
            }
        }
    }
}