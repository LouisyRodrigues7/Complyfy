package com.example.senacplanner.Acoes.Type

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Acao(val id: Int, val nome: String) : Serializable, Parcelable

@Parcelize
data class Atividade(val id:Int ,val nome: String) : Parcelable

@Parcelize
data class AcaoComAtividades(
    val acao: Acao,
    val atividades: List<Atividade>
) : Serializable, Parcelable
