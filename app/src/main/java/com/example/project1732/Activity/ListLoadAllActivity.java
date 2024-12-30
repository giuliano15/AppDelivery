package com.example.project1732.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1732.Adapter.LoadAllAdapter;
import com.example.project1732.Domain.Foods;
import com.example.project1732.databinding.ActivityListLoadAllBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListLoadAllActivity extends BaseActivity {

    private ActivityListLoadAllBinding binding;
    private ArrayList<Foods> list;
    private RecyclerView.Adapter adapterBestFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListLoadAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();
        adapterBestFood = new LoadAllAdapter(list, this);

        loadAllFoods();
        setVariable();

        // Configurar o clique do botão de pesquisa
        binding.searchBtn.setOnClickListener(v -> {
            String text = binding.searchEdtAll.getText().toString();
            if (text.isEmpty()) {
                // Se o texto estiver vazio, carregar todos os alimentos
                loadAllFoods();
            } else {
                // Se houver texto, realizar a busca
                searchFoods(text);
                binding.searchEdtAll.setText("");
            }
        });
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    // Método para carregar todos os alimentos
    public void loadAllFoods() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        Query query = myRef.orderByChild("BestFood");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Limpar a lista existente
                if (snapshot.exists()) {
                    for (DataSnapshot issu : snapshot.getChildren()) {
                        list.add(issu.getValue(Foods.class));
                    }
                    updateRecyclerView();
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void searchFoods(String searchText) {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        Query query = myRef; // Buscar todos os itens inicialmente

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Limpar a lista existente

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);
                        if (food != null) {
                            list.add(food);
                        }
                    }

                    // Filtra a lista localmente para busca insensível a maiúsculas/minúsculas
                    String normalizedSearchText = searchText.toLowerCase();
                    list.removeIf(food -> {
                        // Normaliza o título e compara as 3 primeiras letras
                        String foodTitle = food.getTitle().toLowerCase();
                        return !foodTitle.startsWith(normalizedSearchText);
                    });

                    updateRecyclerView(); // Atualiza o RecyclerView com a lista filtrada
                }

                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Atualiza a RecyclerView com os dados
    private void updateRecyclerView() {
        if (list.size() > 0) {
            binding.loadListAllView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.loadListAllView.setAdapter(adapterBestFood);
        }
    }
}
