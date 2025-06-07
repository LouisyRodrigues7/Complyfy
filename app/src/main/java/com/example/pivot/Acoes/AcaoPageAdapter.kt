package com.example.pivot.Acoes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pivot.Acoes.Type.AcaoComAtividades

/**
 * Adapter para gerenciar páginas (fragments) de ações com suas atividades,
 * utilizado em um ViewPager2.
 *
 * @param fragmentActivity A atividade que hospeda o ViewPager2.
 * @param acoes Lista de objetos [AcaoComAtividades] que serão exibidos nas páginas.
 * @param pilarNome Nome do pilar relacionado às ações (pode ser nulo).
 * @param idUsuario ID do usuário atual, para controle ou filtro das ações.
 * @param idAcao ID da ação atual ou selecionada.
 */
class AcoesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val acoes: List<AcaoComAtividades>,
    private val pilarNome: String?,
    private val idUsuario: Int,
    private val idAcao: Int
) : FragmentStateAdapter(fragmentActivity) {

    /**
     * Retorna o número total de páginas (ações) que o adapter deve gerenciar.
     */
    override fun getItemCount(): Int = acoes.size

    /**
     * Cria um fragment para a posição especificada, inicializando-o com os dados da ação,
     * nome do pilar, ID do usuário e ID da ação.
     *
     * @param position Posição da página/fragment a ser criada.
     * @return O fragment correspondente à ação na posição.
     */
    override fun createFragment(position: Int): Fragment {
        return AcaoPageFragment.newInstance(acoes[position], pilarNome, idUsuario, idAcao)
    }
}
