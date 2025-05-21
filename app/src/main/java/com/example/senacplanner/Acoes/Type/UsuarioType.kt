package com.example.senacplanner.Acoes.Type

data class Usuario(
    val id: Int,
    val nome: String,
    val tipo: String
) {
    override fun toString(): String {
        return "$nome ($tipo)"
    }
}