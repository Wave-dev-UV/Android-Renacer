import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ItemUserBinding
import com.example.gestrenacer.models.User

class UserAdapter(
    private var listaUsers: List<User>,
    private val navController: NavController,
    private val rol: String?
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateList(newList: List<User>) {
        listaUsers = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, navController, rol)
    }

    override fun getItemCount(): Int {
        return listaUsers.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listaUsers[position]
        holder.bind(user)
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val navController: NavController,
        private val rol: String?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val nombre = user.nombre
            val apellido = user.apellido

            binding.lblIniciales.text = "${nombre.firstOrNull() ?: ""}${apellido.firstOrNull() ?: ""}".uppercase()
            binding.txtNombre.text = "$nombre $apellido"
            binding.txtCelular.text = user.celular
            binding.txtRol.text = user.rol
            binding.txtEsLider.text = if (user.esLider) "Si" else "No"


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
