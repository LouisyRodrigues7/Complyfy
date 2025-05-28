package com.example.senacplanner.Acoes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.senacplanner.Acoes.AcaoPageFragment
import com.example.senacplanner.Acoes.Type.AcaoComAtividades

class AcoesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val acoes: List<AcaoComAtividades>,
    private val pilarNome: String?,
    private val idUsuario: Int
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = acoes.size

    override fun createFragment(position: Int): Fragment {
        return AcaoPageFragment.newInstance(acoes[position], pilarNome, idUsuario)
    }
}
