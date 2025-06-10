package com.example.pivot.adapter

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
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.R
import com.example.pivot.model.Notificacao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para gerenciar e exibir uma lista de notificações no RecyclerView.
 * Controla a exibição, interação e atualização visual das notificações,
 * além de lidar com ações específicas ao clicar em cada item.
 *
 * @property lista Lista mutável das notificações exibidas.
 */
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

        val corFundo = when {
            notificacao.tipo_notificacao == TipoNotificacao.ALERTA -> R.color.notificacao_alerta
            notificacao.lida -> R.color.notificacao_lida
            else -> R.color.notificacao_nao_lida
        }

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
                        val db = DatabaseHelper(context)
                        val acao = db.buscarAcaoAprovada(notificacaoAtual.acaoId)
                        if (acao?.aprovado == true) {
                            return@setOnClickListener
                        }
                        abrirDialogAprovacaoAcao(context, notificacaoAtual, adapterPosition, holder)
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

    /**
     * Abre um diálogo para que o usuário aprove uma atividade pendente.
     * Exibe informações relevantes da atividade e seus relacionamentos para melhor decisão.
     *
     * @param context Contexto da aplicação para inflar layouts e acessar o banco.
     * @param notificacaoAtual Notificação que originou o diálogo.
     * @param position Posição da notificação na lista para atualizar após ação.
     * @param holder ViewHolder da notificação para manipulação visual.
     */
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

    /**
     * Abre um diálogo para que o usuário aprove uma atividade pendente.
     * Exibe informações relevantes da atividade e seus relacionamentos para melhor decisão.
     *
     * @param context Contexto da aplicação para inflar layouts e acessar o banco.
     * @param notificacaoAtual Notificação que originou o diálogo.
     * @param position Posição da notificação na lista para atualizar após ação.
     * @param holder ViewHolder da notificação para manipulação visual.
     */
    @SuppressLint("SetTextI18n")
    private fun abrirDialogAprovacaoAcao(
        context: Context,
        notificacaoAtual: Notificacao,
        position: Int,
        holder: NotificacaoAdapter.ViewHolder
    ) {

        val db = DatabaseHelper(context)

        val acao = db.buscarAcaoPorId(notificacaoAtual.acaoId)
        val pilar = db.buscarPilarPorId(acao?.pilar_id ?: 0)
        val usuarioSolicitante = db.obterUsuario(acao?.criadoPorId ?: 0)

        Log.d("DADOS ACAO", acao.toString())

        val dialogView = LayoutInflater.from(context).inflate(R.layout.autorizar_criacao_acao, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvPilar = dialogView.findViewById<TextView>(R.id.tvPilar)
        val tvAcao = dialogView.findViewById<TextView>(R.id.tvAcao)
        val tvSolicitante = dialogView.findViewById<TextView>(R.id.tvSolicitante)
        val tvComentario = dialogView.findViewById<TextView>(R.id.tvComentario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviar)


        tvPilar.text = pilar?.nome
        tvAcao.text = acao?.nome
        tvSolicitante.text = usuarioSolicitante?.nome
        tvComentario.text = ""

        dialog.show()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviar.setOnClickListener {
            db.aprovarAcao(notificacaoAtual.acaoId)
            db.marcarNotificacaoComoLida(notificacaoAtual.id)

            lista[position] = notificacaoAtual.copy(lida = true)
            notifyItemChanged(position)

            dialog.dismiss()

        }

    }

    /**
     * Comportamento padrão para notificações gerais.
     * Atualmente sem implementação.
     *
     * @param context Contexto da aplicação.
     * @param notificacaoAtual Notificação a ser tratada.
     */
    private fun comportamentoPadrao(context: Context, notificacaoAtual: Notificacao) {}


    /**
     * Abre um diálogo para tratar o pedido de conclusão de uma atividade.
     * Permite ao usuário confirmar a finalização da atividade e atualizar status.
     *
     * @param context Contexto da aplicação para inflar layouts e acessar banco.
     * @param notificacaoAtual Notificação que gerou o diálogo.
     * @param position Posição da notificação na lista para atualização.
     * @param holder ViewHolder da notificação para manipulação visual.
     */
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

    /**
     * Converte uma data em formato ISO (timestamp ou yyyy-MM-dd) para o formato brasileiro dd/MM/yyyy.
     * Essa função lida com dois formatos comuns, facilitando exibição para o usuário.
     *
     * @param dataISO Data no formato ISO ou timestamp em String.
     * @return Data formatada em dd/MM/yyyy ou string vazia em caso de erro.
     */
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
