package com.example.senacplanner.Pilares.Type

data class Usuario(
    val id: Int,
    val nome: String,
    val tipo: String
) {
    override fun toString(): String {
        return "$nome ($tipo)"
    }
}