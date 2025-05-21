package com.example.senacplanner.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.model.Notificacao

class NotificacaoAdapter(private val lista: MutableList<Notificacao>) :
    RecyclerView.Adapter<NotificacaoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icone: ImageView = view.findViewById(R.id.iconNotificacao)
        val texto: TextView = view.findViewById(R.id.textoNotificacao)
        val horario: TextView = view.findViewById(R.id.textoHorario)
        val container: View = view.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacao, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificacao = lista[position]
        val context = holder.itemView.context

        holder.texto.text = notificacao.mensagem

        val tempoPassado = DateUtils.getRelativeTimeSpanString(
            notificacao.data,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.horario.text = tempoPassado


        val corFundo = if (notificacao.lida)
            R.color.notificacao_lida
        else
            R.color.notificacao_nao_lida

        holder.container.setBackgroundColor(ContextCompat.getColor(context, corFundo))

        // CLIQUE PARA MARCAR COMO LIDA
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < lista.size) {
                val notificacaoAtual = lista[adapterPosition]

                if (!notificacaoAtual.lida) {
                    val db = DatabaseHelper(context)
                    db.marcarNotificacaoComoLida(notificacaoAtual.id)

                    // Marca como lida e atualiza visualmente
                    lista[adapterPosition] = notificacaoAtual.copy(lida = true)
                    notifyItemChanged(adapterPosition)

                    // Some depois de um tempo
                    holder.itemView.postDelayed({
                        if (adapterPosition < lista.size) {
                            lista.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                        }
                    }, 600)
                }
            }
        }
    }
}

