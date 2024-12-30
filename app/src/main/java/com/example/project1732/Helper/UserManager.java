package com.example.project1732.Helper;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {

    private static UserManager instance;
    private FirebaseUser currentUser;
    private Context context;

    UserManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    public String getUserName() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            return TextUtils.isEmpty(displayName) ? "Usuário" : displayName;
        } else {
            return "Visitante";
        }
    }

    public String getUserEmail() {
        if (currentUser != null) {
            return currentUser.getEmail(); // Recupera o e-mail do usuário
        } else {
            return "Email não disponível";
        }
    }

    public String getUserId() {
        if (currentUser != null) {
            return currentUser.getUid(); // Recupera o ID do usuário
        } else {
            return "ID não disponível";
        }
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public void refreshUser() {
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void showToastIfNotLoggedIn() {
        if (!isUserLoggedIn()) {
            Toast.makeText(context, "Você precisa estar logado para realizar esta ação.", Toast.LENGTH_SHORT).show();
        }
    }
}

