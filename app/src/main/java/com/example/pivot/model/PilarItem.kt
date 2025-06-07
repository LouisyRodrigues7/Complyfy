package com.example.pivot.model

data class PilarItem(
    val id: Int,
    val numero: Int,
    val nome: String
) {
    override fun toString(): String {
        return "Pilar $numero - $nome"
    }
}
