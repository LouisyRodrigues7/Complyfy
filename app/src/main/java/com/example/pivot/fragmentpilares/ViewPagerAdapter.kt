package com.example.pivot.fragmentpilares

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter customizado para uso com o ViewPager2, permitindo alternar entre
 * fragmentos dinamicamente dentro de uma activity (por exemplo, com abas).
 *
 * É utilizado para exibir diferentes visualizações como "Meus Pilares" e
 * "Todos os Pilares", com navegação por título.
 *
 * @constructor Cria um adapter associado à activity pai.
 * @param activity Activity que gerencia o ciclo de vida dos fragments.
 */
class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragmentList = mutableListOf<Fragment>()
    private val titleList = mutableListOf<String>()

    /**
     * Retorna o número de fragments adicionados ao adapter.
     */
    override fun getItemCount(): Int = fragmentList.size

    /**
     * Cria (ou melhor, retorna) o fragmento a ser exibido na posição indicada.
     *
     * @param position Índice do fragmento.
     * @return Fragment correspondente à posição.
     */
    override fun createFragment(position: Int): Fragment = fragmentList[position]

    /**
     * Retorna o título da aba correspondente à posição.
     *
     * @param position Posição do fragmento/título.
     * @return Título da aba associada.
     */
    fun getPageTitle(position: Int): String = titleList[position]

    /**
     * Adiciona dinamicamente um fragmento e seu título ao adapter.
     *
     * @param fragment Fragment a ser exibido.
     * @param title Título exibido na aba correspondente.
     */
    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        titleList.add(title)
    }
}
