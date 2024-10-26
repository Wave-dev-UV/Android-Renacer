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

    fun getSelectedUsers(): List<User> {
        return listaUsers.filterIndexed { index, _ -> selectedUsers[index] == true }
    }

    fun clearSelection() {
        selectedUsers.clear() // Limpiar la selección
        notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
        onSelectedUsersCountChange(getSelectedUsersCount()) // Actualiza el contador al deseleccionar
        onDeleteUsers(false) // Actualiza la visibilidad del botón de eliminar
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
        onSelectedUsersCountChange(getSelectedUsersCount()) // Actualiza el contador al seleccionar/deseleccionar todos
    }

    fun getSelectedUsersCount(): Int {
        return selectedUsers.values.count { it }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol, { isChecked, position ->
            selectUser(parent, position, isChecked)
        }, usersViewModel, this)
    }


    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listaUsers[position]
        if (position == listaUsers.lastIndex) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 300
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 0
            holder.itemView.layoutParams = params
        }
        holder.bind(user, selectedUsers[position] == true, longPressMode) { isChecked ->
            selectUser(holder.itemView, position, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            Log.d("UserAdapter", "Long pressed user at position $position: ${user.nombre}")
            setLongPressMode(true)
            selectUser(holder.itemView, position, true)
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

    private fun selectUser(view: View, position: Int, isChecked: Boolean) {
        selectedUsers[position] = isChecked

        // Actualizamos el ítem después de que el RecyclerView termine de computar su layout
        view.post {
            notifyItemChanged(position)
        }

        val anyUserSelected = selectedUsers.values.any { it }
        Log.d("UserAdapter", "Any user selected: $anyUserSelected")
        onDeleteUsers(anyUserSelected)

        // Nuevo código para contar los seleccionados
        //val selectedCount = selectedUsers.values.count { it }
        val selectedCount = getSelectedUsersCount()
        Log.d("listar2", "Number of selected users: $selectedCount")
        onSelectedUsersCountChange(selectedCount)  // Llamamos al callback para pasar el número de seleccionados
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?,
        private val onCheckedChange: (Boolean, Int) -> Unit,
        private val usersViewModel: UserViewModel,
        private val adapter: UserAdapter
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isSelected: Boolean, longPressMode: Boolean, onCheckedChange: (Boolean) -> Unit) {
            binding.lblIniciales.text = "${user.nombre.firstOrNull()}${user.apellido.firstOrNull()}".uppercase()
            binding.txtNombre.text = "${user.nombre} ${user.apellido}"
            binding.txtCelular.text = user.celular
            binding.txtRol.text = user.rol
            binding.txtEsLider.text = if (user.esLider) "Si" else "No"

            binding.checkboxSelect.visibility = if (longPressMode) View.VISIBLE else View.GONE

            binding.checkboxSelect.setOnCheckedChangeListener(null)
            binding.checkboxSelect.isChecked = isSelected
            binding.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(isChecked)
            }

            if (user.estadoAtencion == "Por Llamar") {
                binding.addPendingUser.setImageResource(R.drawable.filled_notifications);
            } else {
                binding.addPendingUser.setImageResource(R.drawable.notifications);
            }

            manejadorClicCard(user)
            manejadorAnadirPendientes(user)

        }

        private fun manejadorClicCard(user: User){
            if (rol != "Visualizador") {
                binding.cardFeligres.setOnClickListener {
                    val bundle = Bundle().apply {
                        putSerializable("dataFeligres", user)
                        putString("rol", rol)
                    }
                    navController.navigate(R.id.action_listarFragment_to_visualizarUsuarioFragment, bundle)
                }
            } else {
                binding.cardFeligres.setOnClickListener(null)
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

