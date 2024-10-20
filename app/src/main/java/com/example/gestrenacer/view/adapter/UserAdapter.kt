package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ItemUserBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.viewmodel.UserViewModel

class UserAdapter(
    private val listaUsers: List<User>,
    private val navController: NavController,
    private val rol: String?,
    private val usersViewModel: UserViewModel
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol, usersViewModel, this)
    }

    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val feligres = listaUsers[position]

        if (position == listaUsers.lastIndex) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 300
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 0
            holder.itemView.layoutParams = params
        }

        holder.setItemUser(feligres)
    }

    fun changeStatus(position: Int) {
        notifyItemChanged(position)
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?,
        private val usersViewModel: UserViewModel,
        private val adapter: UserAdapter
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

            if (user.estadoAtencion == "Por Llamar") {
                binding.addPendingUser.setImageResource(R.drawable.filled_notifications);
            } else {
                binding.addPendingUser.setImageResource(R.drawable.notifications);
            }

            manejadorClicCard(user)
            manejadorAnadirPendientes(user)
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

        private fun manejadorAnadirPendientes (user:User) {
            binding.addPendingUser.setOnClickListener {

                val currentState = user.estadoAtencion

                if (currentState == "Por Llamar") {
                    user.estadoAtencion = "Llamado"
                    binding.addPendingUser.setImageResource(R.drawable.filled_notifications)
                } else {
                    user.estadoAtencion = "Por Llamar"
                    binding.addPendingUser.setImageResource(R.drawable.notifications)
                }

                usersViewModel.editarUsuario(user)

                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    adapter.changeStatus(position)
                }
            }
        }
    }
}