package com.example.senacplanner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.model.Notificacao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                when (notificacaoAtual.tipo_notificacao) {
                    TipoNotificacao.APROVACAO_ATIVIDADE -> {
                        val db = DatabaseHelper(context)
                        val atividade = db.buscarAtividadeAprovada(notificacaoAtual.atividadeId)
                        if (atividade?.aprovado == true) {
                            return@setOnClickListener
                        }
                        abrirDialogAprovacaoAtividade(context, notificacaoAtual, adapterPosition, holder)
                    }

                    TipoNotificacao.APROVACAO_ACAO -> {
                        //abrirDialogAprovacaoAcao(context, notificacaoAtual)
                    }

                    TipoNotificacao.GERAL -> {
                        comportamentoPadrao(context, notificacaoAtual)
                    }

                    TipoNotificacao.CONCLUSAO_STATUS -> {
                        val db = DatabaseHelper(context)
                        val atividade = db.buscarAtividadePorId(notificacaoAtual.atividadeId)
                        if (atividade?.status == "Finalizada") {
                            return@setOnClickListener
                        }
                        abrirDialogConclusaoStatus(context, notificacaoAtual, adapterPosition, holder)
                    }

                    TipoNotificacao.ALERTA -> {

                    }

                    TipoNotificacao.IMPORTANTE -> {

                    }
                }

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

    @SuppressLint("SetTextI18n")
    private fun abrirDialogAprovacaoAtividade(
        context: Context,
        notificacaoAtual: Notificacao,
        position: Int,
        holder: NotificacaoAdapter.ViewHolder
    ) {

        val db = DatabaseHelper(context)

        val atividade = db.buscarAtividadePorId(notificacaoAtual.atividadeId)
        val usuarioSolicitante = db.obterUsuario(atividade?.criado_por ?: 0)
        val usuarioResponsavel = db.obterUsuario(atividade?.responsavel_id ?: 0)
        val acao = db.buscarAcaoPorId(atividade?.acao_id ?: 0)
        val pilar = db.buscarPilarPorId(acao?.pilar_id ?: 0)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.autorizar_criacao, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvPilar = dialogView.findViewById<TextView>(R.id.tvPilar)
        val tvAcao = dialogView.findViewById<TextView>(R.id.tvAcao)
        val tvSolicitante = dialogView.findViewById<TextView>(R.id.tvSolicitante)
        val tvNomeAtividade = dialogView.findViewById<TextView>(R.id.tvNomeAtividade)
        val tvResponsavel = dialogView.findViewById<TextView>(R.id.tvResponsavel)
        val tvDataInicio = dialogView.findViewById<TextView>(R.id.tvDataInicio)
        val tvDataConclusao = dialogView.findViewById<TextView>(R.id.tvDataConclusao)
        val tvComentario = dialogView.findViewById<TextView>(R.id.tvComentario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviar)


        Log.d("Atividade dados", atividade.toString())

        tvPilar.text = "Pilar: ${pilar?.nome}"
        tvAcao.text = "Ação: ${acao?.nome}"
        tvSolicitante.text = usuarioSolicitante?.nome ?: "-"
        tvNomeAtividade.text = atividade?.nome ?: "-"
        tvResponsavel.text = usuarioResponsavel?.nome ?: "-"
        tvDataInicio.text = formatarDataParaBR(atividade?.data_inicio ?: "-")
        tvDataConclusao.text = formatarDataParaBR(atividade?.data_conclusao ?: "-")
        tvComentario.text = ""

        dialog.show()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviar.setOnClickListener {
            db.aprovarAtividade(notificacaoAtual.atividadeId)
            db.marcarNotificacaoComoLida(notificacaoAtual.id)

            lista[position] = notificacaoAtual.copy(lida = true)
            notifyItemChanged(position)

            dialog.dismiss()

        }

    }

    private fun comportamentoPadrao(context: Context, notificacaoAtual: Notificacao) {}


    @SuppressLint("SetTextI18n")
    private fun abrirDialogConclusaoStatus(
        context: Context,
        notificacaoAtual: Notificacao,
        position: Int,
        holder: NotificacaoAdapter.ViewHolder
    ) {

        val db = DatabaseHelper(context)

        Log.d("ID ATIVIDADE", notificacaoAtual.atividadeId.toString())

        val atividade = db.buscarAtividadePorId(notificacaoAtual.atividadeId)
        val usuarioResponsavel = db.obterUsuario(atividade?.responsavel_id ?: 0)
        val acao = db.buscarAcaoPorId(atividade?.acao_id ?: 0)
        val pilar = db.buscarPilarPorId(acao?.pilar_id ?: 0)

        Log.d("dados para concluir atividade", atividade.toString())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.pedido_conclusao, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvPilar = dialogView.findViewById<TextView>(R.id.tvNomePilar)
        val tvAcao = dialogView.findViewById<TextView>(R.id.tvNomeAcao)
        val tvNomeAtividade = dialogView.findViewById<TextView>(R.id.tvNomeAtividade)
        val tvResponsavel = dialogView.findViewById<TextView>(R.id.tvNomeResponsavel)
        val tvDataInicio = dialogView.findViewById<TextView>(R.id.tvDataInicio)
        val tvDataConclusao = dialogView.findViewById<TextView>(R.id.tvDataConclusao)
        val tvComentario = dialogView.findViewById<EditText>(R.id.etComentario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviar)

        tvPilar.text = "Pilar: ${pilar?.nome}"
        tvAcao.text = "Ação: ${acao?.nome}"
        tvNomeAtividade.text = atividade?.nome ?: "-"
        tvResponsavel.text = usuarioResponsavel?.nome
        tvDataInicio.text = formatarDataParaBR(atividade?.data_inicio ?: "-")
        tvDataConclusao.text = formatarDataParaBR(atividade?.data_conclusao ?: "-")

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviar.setOnClickListener {
            db.atualizarStatus(notificacaoAtual.atividadeId?.toInt() ?: 0, "Finalizada")
            db.marcarNotificacaoComoLida(notificacaoAtual.id)
            dialog.dismiss()

            lista[position] = notificacaoAtual.copy(lida = true)
            notifyItemChanged(position)

        }

        dialog.show()

    }

    fun formatarDataParaBR(dataISO: String): String {
        return try {
            val timestamp = dataISO.toLongOrNull()
            val data: Date? = if (timestamp != null) {
                Date(timestamp)
            } else {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                formatoEntrada.parse(dataISO)
            }
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (data != null) formatoSaida.format(data) else ""
        } catch (e: Exception) {
            ""
        }

    }

}
