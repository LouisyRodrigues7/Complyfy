package com.example.senacplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.R
import com.example.senacplanner.Pilar  // Verifique se o pacote está correto

class PilarAdapter(
    private val pilares: List<Pilar>,
    private val onClick: (Pilar) -> Unit  // Função de clique recebida como parâmetro
) : RecyclerView.Adapter<PilarAdapter.PilarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PilarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pilar, parent, false)
        return PilarViewHolder(view)
    }

    override fun onBindViewHolder(holder: PilarViewHolder, position: Int) {
        val pilar = pilares[position]
        holder.numeroPilar.text = pilar.numero.toString()  // Preenche o número
        holder.tituloPilar.text = pilar.titulo  // Preenche o título

        // Define o clique para chamar a função onClick
        holder.itemView.setOnClickListener {
            onClick(pilar)  // Chama a função de clique, passando o Pilar
        }
    }

    override fun getItemCount(): Int = pilares.size

    class PilarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numeroPilar: TextView = itemView.findViewById(R.id.numeroPilarGrande)
        val tituloPilar: TextView = itemView.findViewById(R.id.textoPilarGrande)
    }
}
