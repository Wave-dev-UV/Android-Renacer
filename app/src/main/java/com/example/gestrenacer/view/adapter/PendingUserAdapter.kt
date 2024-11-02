package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ItemPendingUserBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.viewmodel.UserViewModel

class PendingUserAdapter(
    private var listaUsers: MutableList<User>,
    private val navController: NavController,
    private val rol: String?,
    private val usersViewModel: UserViewModel,
    private val setResSize: (List<User>) -> Unit,
    private val showNoContent: (List<User>) -> Unit
): RecyclerView.Adapter<PendingUserAdapter.UserViewHolder>() {

    private var originalList: MutableList<User>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemPendingUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol, usersViewModel, this)
    }

    fun updateList(newList: MutableList<User>, original: MutableList<User>? = null) {
        listaUsers = newList
        originalList = original
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val feligres = listaUsers[position]
        holder.setItemUser(feligres)

    }

    fun delete(position: Int) {
        listaUsers.removeAt(position)

        if (originalList!=null){
            originalList?.removeAt(position)
        }

        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaUsers.size)
        setResSize(listaUsers)
        showNoContent(listaUsers)
    }


    class UserViewHolder(
        private val binding: ItemPendingUserBinding,
        private val navController: NavController,
        private val rol: String?,
        private val usersViewModel: UserViewModel,
        private val adapter: PendingUserAdapter
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
            manejadorEliminarPendientes(user)

        }

        private fun manejadorClicCard(user: User){
            if (rol != "Visualizador"){
                binding.cardPendingFeligres.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putSerializable("dataFeligres",user)
                    bundle.putString("rol",rol)
                    navController.navigate(R.id.action_pendingFragment_to_visualizarUsuarioFragment, bundle)
                }
            }
        }

        private fun manejadorEliminarPendientes (user:User) {
            binding.removePendingUser.setOnClickListener {
                user.estadoAtencion = "Llamado"
                usersViewModel.editarUsuario(user, llamado = true)

                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    adapter.delete(position)
                }
            }
        }


    }
}