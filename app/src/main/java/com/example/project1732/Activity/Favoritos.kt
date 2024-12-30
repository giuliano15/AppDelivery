package com.example.project1732.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project1732.Adapter.FavoriteAdapter
import com.example.project1732.Domain.Foods
import com.example.project1732.Helper.FavoriteManager
import com.example.project1732.databinding.ActivityFavoritosBinding

class Favoritos : AppCompatActivity() {

    private val binding by lazy {
        ActivityFavoritosBinding.inflate(layoutInflater)
    }
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var favoriteManager: FavoriteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            val intent = Intent(this, Principal::class.java)
            startActivity(intent)
        }

        favoriteManager = FavoriteManager(this)

        // Carregar favoritos do Firebase e atualizar o RecyclerView
        loadFavoritesFromFirebase()

        // Recupera o objeto passado pela Intent
        val favorite = intent.getSerializableExtra("favorite") as? Foods
        if (favorite != null) {
            // Adiciona o objeto aos favoritos e salva no Firebase
            favoriteManager.addFavorite(favorite)
        }

        // Configura o RecyclerView usando View Binding
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoriteAdapter =
            FavoriteAdapter(this, favoriteManager.getFavorites(), this, favoriteManager)
        binding.favoritesRecyclerView.adapter = favoriteAdapter

        updateEmptyTextVisibility(favoriteManager.getFavorites().isEmpty())

        setupSearchFilter()
    }

    private fun loadFavoritesFromFirebase() {
        favoriteManager.loadFavoritesFromFirebase { favorites ->
            // Atualize o RecyclerView com a lista de favoritos recebida do Firebase
            favoriteAdapter.updateFavorites(favorites)
            updateEmptyTextVisibility(favorites.isEmpty())
        }
    }

    private fun updateEmptyTextVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
        }
    }

    // Método para configurar o filtro de busca
    private fun setupSearchFilter() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não precisa fazer nada aqui
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aplica o filtro ao texto inserido
                val searchText = s.toString()
                favoriteAdapter.filterFavorites(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // Não precisa fazer nada aqui
            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, Principal::class.java)
        startActivity(intent)
    }
}

