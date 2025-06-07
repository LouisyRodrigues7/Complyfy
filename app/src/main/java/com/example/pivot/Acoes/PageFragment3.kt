package com.example.pivot.Acoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pivot.R

/**
 * Fragmento responsável por exibir a terceira página de conteúdo na navegação por páginas.
 *
 * Geralmente utilizado em ViewPager ou estruturas similares dentro de uma única Activity,
 * este fragmento carrega o layout definido em `fragment_page_3.xml`.
 */
class PageFragment3 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_3, container, false)
    }
}