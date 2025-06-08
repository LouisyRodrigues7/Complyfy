package com.example.pivot.model

/**
 * Representa um Pilar estratégico no sistema.
 *
 * Essa classe é utilizada para exibir e identificar pilares dentro da interface,
 * principalmente em listas ou seletores (spinners), associando um nome legível
 * a um número identificador.
 *
 * @property titulo Nome descritivo do pilar (ex: "Educação", "Infraestrutura").
 * @property numero Número sequencial ou identificador do pilar.
 */
data class Pilar(
    val titulo: String,  // Aqui 'titulo' é o nome do pilar
    val numero: Int
)
