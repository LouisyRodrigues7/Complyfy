package com.example.pivot.Acoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pivot.R

/**
 * Fragmento responsável por exibir a segunda página de conteúdo em uma navegação entre páginas.
 *
 * Pode ser usado em um ViewPager ou outra estrutura de múltiplas telas dentro de uma mesma Activity.
 * O layout associado (`fragment_page_2.xml`) define os elementos visuais desta página.
 */
class PageFragment2 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_2, container, false)
    }
}