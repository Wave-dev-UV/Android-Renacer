package com.example.gestrenacer.view.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ItemUserBinding
import com.example.gestrenacer.models.User
import com.example.gestrenacer.viewmodel.UserViewModel

class UserAdapter(
    private var listaUsers: List<User>,
    private val navController: NavController,
    private val rol: String?,
    private val usersViewModel: UserViewModel,
    private val onDeleteUsers: (Boolean) -> Unit,
    private val onSelectedUsersCountChange: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val selectedUsers = mutableMapOf<Int, Boolean>()
    private var longPressMode = false

    fun getSelectedUsers(): List<User> = listaUsers.filterIndexed { index, _ -> selectedUsers[index] == true }

    fun clearSelection() {
        selectedUsers.clear()
        notifyDataSetChanged()
        onSelectedUsersCountChange(getSelectedUsersCount())
        onDeleteUsers(false)
    }

    fun updateList(newList: List<User>) {
        listaUsers = newList
        notifyDataSetChanged()
    }

    fun selectAll(shouldSelect: Boolean) {
        listaUsers.forEachIndexed { index, _ ->
            selectedUsers[index] = shouldSelect
        }
        notifyDataSetChanged()
        onSelectedUsersCountChange(getSelectedUsersCount())
    }

    fun getSelectedUsersCount(): Int {
        val count = selectedUsers.values.count { it }
        Log.d("UserAdapter", "Número de usuarios seleccionados: $count")
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol, usersViewModel, this)
    }

    override fun getItemCount(): Int = listaUsers.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listaUsers[position]
        val isSelected = selectedUsers[position] == true

        holder.bind(user, isSelected, longPressMode && rol == "Administrador") { isChecked ->
            selectUser(position, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            if (rol == "Administrador") {
                setLongPressMode(true)
                selectUser(position, true)
            }
            true
        }
    }

    fun changeStatus(position: Int) {
        notifyItemChanged(position)
    }

    fun setLongPressMode(enabled: Boolean) {
        longPressMode = enabled
        notifyDataSetChanged()
    }

    private fun selectUser(position: Int, isChecked: Boolean) {
        selectedUsers[position] = isChecked
        notifyItemChanged(position)

        val anyUserSelected = selectedUsers.containsValue(true)
        onDeleteUsers(anyUserSelected)

        val selectedCount = getSelectedUsersCount()
        onSelectedUsersCountChange(selectedCount)

        if (selectedCount == 0) {
            notifyDataSetChanged()
        }
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?,
        private val usersViewModel: UserViewModel,
        private val adapter: UserAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isSelected: Boolean, longPressMode: Boolean, onCheckedChange: (Boolean) -> Unit) {
            binding.lblIniciales.text = "${user.nombre.firstOrNull()}${user.apellido.firstOrNull()}".uppercase()
            binding.txtNombre.text = "${user.nombre} ${user.apellido}"
            binding.txtCelular.text = user.celular
            binding.txtRol.text = user.rol
            binding.txtEsLider.text = if (user.esLider) "Si" else "No"

            binding.checkboxSelect.apply {
                visibility = if (adapter.getSelectedUsersCount() > 0) View.VISIBLE else View.GONE
                setOnCheckedChangeListener(null)  // Eliminar temporalmente listener
                isChecked = isSelected
                setOnCheckedChangeListener { _, isChecked -> onCheckedChange(isChecked) }
            }

            // Configuración de icono y visibilidad de `addPendingUser`
            binding.addPendingUser.apply {
                visibility = if (adapter.getSelectedUsersCount() == 0) View.VISIBLE else View.GONE
                setImageResource(if (user.estadoAtencion == "Por Llamar") R.drawable.filled_notifications else R.drawable.notifications)
            }


            manejadorClicCard(user)
            manejadorAnadirPendientes(user)
        }

        private fun manejadorClicCard(user: User) {
            if (rol != "Visualizador") {
                binding.cardFeligres.setOnClickListener {
                    try {
                        val bundle = Bundle().apply {
                            putSerializable("dataFeligres", user)
                            putString("rol", rol)
                        }
                        navController.navigate(R.id.action_listarFragment_to_visualizarUsuarioFragment, bundle)
                    } catch (e: Exception) {
                        Log.e("UserAdapter", "Error navigating: ${e.message}")
                    }
                }
            } else {
                binding.cardFeligres.setOnClickListener(null)
            }
        }

        private fun manejadorAnadirPendientes(user: User) {
            binding.addPendingUser.setOnClickListener {
                user.estadoAtencion = if (user.estadoAtencion == "Por Llamar") "Llamado" else "Por Llamar"
                usersViewModel.editarUsuario(user)
                adapter.changeStatus(bindingAdapterPosition)
            }
        }
    }
}
