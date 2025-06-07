package com.example.senacplanner.apoio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.R
import com.example.senacplanner.model.Pilar
import com.example.senacplanner.adapter.PilarAdapter

/**
 * Fragmento responsável por exibir a lista de pilares específicos do perfil Apoio.
 *
 * A lista é fixa (hardcoded) com pilares que representam as áreas de atuação do Apoio,
 * servindo para filtrar ou apresentar apenas os pilares relevantes a esse perfil.
 *
 * A interface utiliza um RecyclerView para apresentar os pilares,
 * permitindo interação básica com cliques nos itens (ainda sem navegação).
 */

class MeusPilaresApoioFragment : Fragment() {

    /**
     * RecyclerView que exibe a lista de pilares do Apoio.
     */
    private lateinit var recyclerView: RecyclerView

    /**
     * Adapter responsável por popular o RecyclerView com os dados dos pilares.
     */
    private lateinit var adapter: PilarAdapter

    // Pilares do apoio
    private val pilaresApoio = listOf(
        Pilar("Transparência", 1),
        Pilar("Investigações Internas", 2)
    )

    /**
     * Cria a view do fragmento, configurando o layout e o RecyclerView.
     *
     * O layout é inflado a partir do XML padrão de listagem de pilares.
     * O RecyclerView é configurado com layout linear vertical e o adapter recebe a lista
     * estática de pilares do Apoio.
     *
     * A ação de clique nos itens está configurada para registrar no console qual pilar foi clicado,
     * sem navegação adicional.
     *
     * @param inflater Inflater usado para inflar o layout do fragmento.
     * @param container ViewGroup pai onde o fragmento será inserido.
     * @param savedInstanceState Estado salvo para restauração da UI, se houver.
     * @return View criada para este fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)

        // Configura o RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPilaresApoio)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PilarAdapter(pilaresApoio) { pilar ->
            println("Clicou no pilar: ${pilar.titulo}")
        }
        recyclerView.adapter = adapter

        return view
    }
}
