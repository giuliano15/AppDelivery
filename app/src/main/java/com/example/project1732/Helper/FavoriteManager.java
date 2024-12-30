//package com.example.project1732.Helper;
package com.example.project1732.Helper;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.project1732.Domain.Foods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private ArrayList<Foods> favoriteList;
    private Context context;
    private TinyDB tinyDB;

    public FavoriteManager(Context context) {
        this.context = context;
        tinyDB = new TinyDB(context);
        loadFavorites(); // Carregar favoritos locais
    }

    public void addFavorite(Foods food) {
        if (!isFavorite(food)) {
            favoriteList.add(food);
            saveFavorites();
            // Também adicione ao Firebase
            saveFavoriteToFirebase(food);
            Toast.makeText(context, "Item adicionado aos favoritos", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Item já está nos favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeFavorite(Foods food) {
        favoriteList.remove(food);
        saveFavorites();
        // Também remova do Firebase
        removeFavoriteFromFirebase(food);
    }

    public List<Foods> getFavorites() {
        return favoriteList;
    }

    private void saveFavorites() {
        tinyDB.putListObject("FavoritesList", favoriteList);
    }

    private void loadFavorites() {
        favoriteList = tinyDB.getListObject("FavoritesList", Foods.class);
        if (favoriteList == null) {
            favoriteList = new ArrayList<>();
        }
    }

    public boolean isFavorite(Foods food) {
        for (Foods favorite : favoriteList) {
            if (favorite.getTitle().equals(food.getTitle())) {
                return true;
            }
        }
        return false;
    }

    public void loadFavoritesFromFirebase(final FirebaseCallback callback) {
        String userName = UserManager.getInstance(context).getUserName();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userName)
                .child("favorites")
                .child(userId);

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favoriteList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Foods food = snapshot.getValue(Foods.class);
                    if (food != null) {
                        favoriteList.add(food);
                    }
                }
                callback.onCallback(favoriteList);
                saveFavorites(); // Opcional, se você ainda quiser salvar localmente
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratar erro
            }
        });
    }

    // Interface para callback
    public interface FirebaseCallback {
        void onCallback(List<Foods> favorites);
    }

    private void saveFavoriteToFirebase(Foods food) {
        String userName = UserManager.getInstance(context).getUserName();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userName)
                .child("favorites")
                .child(userId)
                .child(String.valueOf(food.getId())); // Use o ID do alimento como chave

        favoritesRef.setValue(food);
    }

    private void removeFavoriteFromFirebase(Foods food) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = UserManager.getInstance(context).getUserName();
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userName)
                .child("favorites")
                .child(userId)
                .child(String.valueOf(food.getId())); // Use o ID do alimento como chave

        favoritesRef.removeValue();
    }
}


