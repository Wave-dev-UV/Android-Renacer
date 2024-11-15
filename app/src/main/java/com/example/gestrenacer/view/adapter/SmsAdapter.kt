package com.example.gestrenacer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.gestrenacer.databinding.ItemSmsBinding
import com.example.gestrenacer.models.Sms

class SmsAdapter(
    private val listaSms: List<Sms>
) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val binding = ItemSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaSms.size
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val feligres = listaSms[position]
        holder.setCardSms(feligres)
    }

    class SmsViewHolder(
        private val binding: ItemSmsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setCardSms(sms: Sms) {
            val fecha = sms.fecha.toDate().toString().split(" ")
            val filtros = sms.filtros

            binding.txtFecha.text = "${fecha[0]}, ${fecha[1]} ${fecha[2]} de ${fecha[5]}."
            binding.txtMensaje.text = sms.mensaje
            binding.txtEnviado.text = sms.enviado

            if (filtros.isNotEmpty()) {
                mostrarChip(filtros)
            }
        }

        fun mostrarChip(filtros: List<String>) {
            for (i in filtros) {
                val aux = i.split(" ")
                when (aux[0]) {
                    "Casado(a)." -> binding.chipCasado.isVisible = true
                    "Viudo(a)." -> binding.chipViudo.isVisible = true
                    "Masculino." -> binding.chipMasculino.isVisible = true
                    "Femenino." -> binding.chipFemenino.isVisible = true
                    "Soltero(a)." -> binding.chipSoltero.isVisible = true
                    "Unión" -> binding.chipLibre.isVisible = true
                    "Divorciado(a)." -> binding.chipDivorciado.isVisible = true
                    "Menores" -> {
                        binding.chipEdadMin.setText(i)
                        binding.chipEdadMin.isVisible = true
                    }

                    "Mayores" -> {
                        binding.chipEdadMax.setText(i)
                        binding.chipEdadMax.isVisible = true
                    }

                    "Todas" -> binding.chipTodaEdad.isVisible = true
                }
            }
        }
    }
}