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

class NotificacaoAdapter(private val lista: List<Notificacao>) :
    RecyclerView.Adapter<NotificacaoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icone: ImageView = view.findViewById(R.id.iconNotificacao)
        val texto: TextView = view.findViewById(R.id.textoNotificacao)
        val horario: TextView = view.findViewById(R.id.textoHorario)
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

        // Estilo visual para notificação lida/não lida
        if (notificacao.lida) {
            holder.itemView.alpha = 0.5f // deixa mais "apagada"
        } else {
            holder.itemView.alpha = 1f
        }

        // Clique para marcar como lida
        holder.itemView.setOnClickListener {
            val dbHelper = DatabaseHelper(context)
            dbHelper.marcarNotificacaoComoLida(notificacao.id)

            Toast.makeText(context, "Notificação marcada como lida", Toast.LENGTH_SHORT).show()

            // Atualiza visual local (mas você teria que recarregar a lista na Activity/Fragment para refletir completamente)
            holder.itemView.alpha = 0.5f
        }
    }
}
