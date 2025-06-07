package com.example.senacplanner.Acoes.Type

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Representa uma Ação dentro do sistema.
 *
 * @property id Identificador único da ação.
 * @property nome Nome descritivo da ação.
 * @property pilarId Identificador do pilar ao qual esta ação pertence.
 */
@Parcelize
data class Acao(val id: Int, val nome: String, val pilarId: Int) : Serializable, Parcelable

/**
 * Representa uma Atividade vinculada a uma ação.
 *
 * @property id Identificador único da atividade.
 * @property nome Nome da atividade.
 * @property status Status atual da atividade (ex.: "Em andamento", "Finalizada").
 * @property aprovado Indica se a atividade foi aprovada (true) ou não (false).
 */
@Parcelize
data class Atividade(val id:Int ,val nome: String, val status: String, val aprovado: Boolean) : Parcelable

/**
 * Agrega uma ação e suas atividades relacionadas.
 *
 * @property acao Objeto [Acao] que contém os dados da ação.
 * @property atividades Lista de [Atividade]s associadas à ação.
 */
@Parcelize
data class AcaoComAtividades(
    val acao: Acao,
    val atividades: List<Atividade>
) : Serializable, Parcelable
