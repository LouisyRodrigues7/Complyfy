package com.example.senacplanner.Pilares

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.R

class ListaAtividadesActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        viewPager = findViewById(R.id.viewPager)
        val adapter = ListaAtividadesAdapter(this)
        viewPager.adapter = adapter

        val btnAdicionarAtividade: Button = findViewById(R.id.btnAdicionarAtividade)
        btnAdicionarAtividade.setOnClickListener {
            val intent = Intent(this, CriarAtividadeActivity::class.java)
            startActivity(intent)
        }
    }
}
