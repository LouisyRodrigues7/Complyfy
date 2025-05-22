package com.example.senacplanner.editarAtividade

data class AtividadeEdit(val id:Int ,val nome: String, val status: String, val data_inicio: String,
                         val data_conclusao: String, val responsavel_id: Int)
