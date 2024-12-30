package com.example.project1732.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.project1732.R
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Adiciona um delay de 2 segundos antes de verificar o login
        Handler(Looper.getMainLooper()).postDelayed({
            val usuarioLogado = verificarUsuarioLogado()

            if (usuarioLogado) {
                // Usuário está logado, navega para a tela principal
                val intent = Intent(this, Principal::class.java)
                startActivity(intent)
                finish()
            } else {
                // Usuário não está logado, navega para a tela de login
                navegarParaLogin()
            }
        }, 3000) // Delay de 5 segundos (2000 milissegundos)
    }

    private fun verificarUsuarioLogado(): Boolean {
        val usuarioAtual = FirebaseAuth.getInstance().currentUser
        return usuarioAtual != null
    }

    private fun navegarParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finaliza a atividade de login
    }
}