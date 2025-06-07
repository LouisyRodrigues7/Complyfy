package com.example.pivot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pivot.R
import com.example.pivot.model.Pilar

/**
 * Adapter para exibir uma lista de pilares em um RecyclerView.
 *
 * @property pilares Lista de objetos [Pilar] que serão exibidos.
 * @property onClick Função de callback executada ao clicar em um item da lista,
 * recebendo o [Pilar] clicado.
 */
class PilarAdapter(
    private val pilares: List<Pilar>,
    private val onClick: (Pilar) -> Unit
) : RecyclerView.Adapter<PilarAdapter.PilarViewHolder>() {


    /**
     * Cria e infla a view de cada item da lista de pilares.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PilarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pilar, parent, false)
        return PilarViewHolder(view)
    }

    /**
     * Vincula os dados do pilar à view, preenchendo número e título.
     * Também configura o listener para cliques, repassando o pilar selecionado.
     */
    override fun onBindViewHolder(holder: PilarViewHolder, position: Int) {
        val pilar = pilares[position]
        holder.numeroPilar.text = pilar.numero.toString()
        holder.tituloPilar.text = pilar.titulo
        holder.itemView.setOnClickListener {
            onClick(pilar)
        }
    }


    /**
     * Retorna o total de itens na lista de pilares.
     */
    override fun getItemCount(): Int = pilares.size


    /**
     * ViewHolder que mantém referências das views do item_pilar para melhorar a performance.
     *
     * @property numeroPilar TextView que exibe o número do pilar.
     * @property tituloPilar TextView que exibe o título do pilar.
     */
    class PilarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numeroPilar: TextView = itemView.findViewById(R.id.numeroPilarGrande)
        val tituloPilar: TextView = itemView.findViewById(R.id.textoPilarGrande)
    }
}
