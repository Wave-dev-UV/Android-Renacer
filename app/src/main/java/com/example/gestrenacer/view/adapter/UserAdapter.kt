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

class UserAdapter(
    private var listaUsers: List<User>,
    private val navController: NavController,
    private val rol: String?,
    private val onDeleteUsers: (Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val selectedUsers = mutableMapOf<Int, Boolean>()
    private var longPressMode = false

    fun getSelectedUsers(): List<User> {
        return listaUsers.filterIndexed { index, _ -> selectedUsers[index] == true }
    }

    fun updateList(newList: List<User>) {
        listaUsers = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol) { isChecked, position ->
            selectUser(parent, position, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listaUsers[position]
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

    fun setLongPressMode(enabled: Boolean) {
        longPressMode = enabled
        notifyDataSetChanged()
    }

    private fun selectUser(view: View, position: Int, isChecked: Boolean) {
        Log.d("UserAdapter", "SelectUser called with position: $position, isChecked: $isChecked")

        selectedUsers[position] = isChecked

        // Actualizamos el ítem después de que el RecyclerView termine de computar su layout
        view.post {
            notifyItemChanged(position)
        }

        Log.d("UserAdapter", "Selected users: $selectedUsers")

        val anyUserSelected = selectedUsers.values.any { it }
        Log.d("UserAdapter", "Any user selected: $anyUserSelected")
        onDeleteUsers(anyUserSelected)
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?,
        private val onCheckedChange: (Boolean, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isSelected: Boolean, longPressMode: Boolean, onCheckedChange: (Boolean) -> Unit) {
            binding.lblIniciales.text = "${user.nombre.firstOrNull()}${user.apellido.firstOrNull()}".uppercase()
            binding.txtNombre.text = "${user.nombre} ${user.apellido}"
            binding.txtCelular.text = user.celular
            binding.txtRol.text = user.rol
            binding.txtEsLider.text = if (user.esLider) "Si" else "No"

            binding.checkboxSelect.visibility = if (longPressMode) View.VISIBLE else View.GONE
            binding.checkboxSelect.isChecked = isSelected

            binding.checkboxSelect.setOnCheckedChangeListener(null)
            binding.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(isChecked)
            }

            if (rol != "Visualizador") {
                binding.cardFeligres.setOnClickListener {
                    val bundle = Bundle().apply {
                        putSerializable("dataFeligres", user)
                        putString("rol", rol)
                    }
                    navController.navigate(R.id.action_listarFragment_to_editarUsuarioFragment, bundle)
                }
            } else {
                binding.cardFeligres.setOnClickListener(null)
            }
        }
    }
}

