package com.example.project1732.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project1732.Helper.NetworkUtils;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends BaseActivity {

    private EditText editTextEmailLogin, editTextSenhaLogin;
    private Button btnLogin;
    private TextView textoTelaCadastro;
    private ProgressBar progressBar;
    private String[] mensagens = {"Preencha todos os campos", "Login efetuado com sucesso"};
    private static final String TAG = "LoginActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa os componentes
        iniciarComponentes();

        // Configura o clique no TextView para abrir a tela de cadastro
        textoTelaCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        // Configura o clique no botão de login
        btnLogin.setOnClickListener(v -> {
            String emailLogin = editTextEmailLogin.getText().toString();
            String senhaLogin = editTextSenhaLogin.getText().toString();

            if (emailLogin.isEmpty() || senhaLogin.isEmpty()) {
                Snackbar snackbar = Snackbar.make(v, mensagens[0], Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.RED);
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();
            } else {
                autenticarUsuario(v, emailLogin, senhaLogin);
            }
        });
    }

    private void autenticarUsuario(View view, String emailLogin, String senhaLogin) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailLogin, senhaLogin)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.VISIBLE); // Colocar aqui para que o ProgressBar apareça antes da tentativa de autenticação
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        UserManager.getInstance(LoginActivity.this).refreshUser(); // Atualize o UserManager
                        String displayName = user != null ? user.getDisplayName() : "Usuário";

                        // Chama a função para salvar o token FCM no Realtime Database
                        saveFCMToken(username);

                        // Obtendo o token FCM
//                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
//                            if (tokenTask.isSuccessful()) {
//                                String token = tokenTask.getResult();
//                                // Atualiza o token no Firestore
//                                FirebaseFirestore db = FirebaseFirestore.getInstance();
//                                DocumentReference documentReference = db.collection("Usuarios").document(user.getUid());
//
//                                documentReference.update("deviceToken", token)
//                                        .addOnSuccessListener(aVoid -> Log.d("Token", "Token FCM atualizado com sucesso"))
//                                        .addOnFailureListener(e -> Log.d("Token", "Falha ao atualizar token FCM: " + e.toString()));
//                            } else {
//                                Log.w("FCM", "Failed to get FCM token", tokenTask.getException());
//                            }
//                        });

                        new Handler().postDelayed(() -> {
                            progressBar.setVisibility(View.GONE); // Ocultar o ProgressBar após o processamento
                            Toast.makeText(LoginActivity.this, "Bem-vindo, " + displayName, Toast.LENGTH_SHORT).show();
                            telaPrincipal();
                        }, 1000);
                    } else {
                        progressBar.setVisibility(View.GONE); // Ocultar o ProgressBar também em caso de falha
                        String erro;
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            erro = "Erro ao logar";
                        }
                        Snackbar snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_LONG);
                        snackbar.setBackgroundTint(Color.RED);
                        snackbar.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });
    }

    public static void saveFCMToken(String username) {
        // Obtém o token FCM do dispositivo
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();

                // Verifica se o nome de usuário e o token foram obtidos
                if (username != null && token != null) {
                    // Referencia o nó "users" com o nome de usuário no Realtime Database
                    DatabaseReference userFormRef = FirebaseDatabase.getInstance().getReference("users").child(username);

                    // Mapa com o deviceToken para ser salvo no nó do usuário
                    userFormRef.child("deviceToken").setValue(token)
                            .addOnSuccessListener(aVoid -> {
                                // Token salvo com sucesso
                                Log.d("FCM", "Token salvo com sucesso");
                            })
                            .addOnFailureListener(e -> {
                                // Falha ao salvar o token
                                Log.e("FCM", "Erro ao salvar o token", e);
                            });
                }
            } else {
                Log.w("FCM", "Falha ao obter o token FCM", task.getException());
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual != null) {
            telaPrincipal();
        }
    }

    private void telaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, Principal.class);
        startActivity(intent);
        finish(); // Adicionado para finalizar a atividade de login
    }

    private void iniciarComponentes() {
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextSenhaLogin = findViewById(R.id.editTextSenhaLogin);
        btnLogin = findViewById(R.id.btnLogar);
        progressBar = findViewById(R.id.progressBarLogin);
        textoTelaCadastro = findViewById(R.id.textViewTelaCadastro);
    }
}

