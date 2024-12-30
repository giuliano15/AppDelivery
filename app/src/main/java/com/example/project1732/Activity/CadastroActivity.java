package com.example.project1732.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project1732.Helper.NetworkUtils;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends BaseActivity {

    private EditText editTextNome, editTextEmail, editTextSenha;
    private Button btnCadastrar;
    private String[] mensagens = {"Preencha todos os campos", "Cadastro realizado com sucesso"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        iniciarComponentes();

        btnCadastrar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString();
            String email = editTextEmail.getText().toString();
            String senha = editTextSenha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Snackbar snackbar = Snackbar.make(v, mensagens[0], Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE);
                snackbar.setTextColor(Color.BLACK);
                snackbar.show();
            } else {
                cadastrarUsuario(v);
            }
        });
    }

    private void cadastrarUsuario(View v) {
        String email = editTextEmail.getText().toString();
        String senha = editTextSenha.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(this, task -> {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show();
                return;
            }
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Atualizar o nome no perfil do Firebase Authentication
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(editTextNome.getText().toString())
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("Profile", "User profile updated.");

                            // Agora que o perfil foi atualizado, salve os dados no Firestore
                            salvarDadosUsuario();

                            // Atualize o UserManager após o cadastro
                            UserManager.getInstance(CadastroActivity.this).refreshUser();

                            // Navega para a tela de login
                            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                            startActivity(intent);

                            Snackbar snackbar = Snackbar.make(v, mensagens[1], Snackbar.LENGTH_LONG);
                            snackbar.setBackgroundTint(Color.GREEN);
                            snackbar.setTextColor(Color.WHITE);
                            snackbar.show();
                        } else {
                            Log.e("Profile", "Profile update failed.");
                        }
                    });
                }
            } else {
                String erro;
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException e) {
                    erro = "Crie uma senha com no mínimo seis caracteres";
                } catch (FirebaseAuthUserCollisionException e) {
                    erro = "Conta já cadastrada";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    erro = "Email inválido";
                } catch (Exception e) {
                    erro = "Erro no cadastro de usuário";
                }

                Snackbar snackbar = Snackbar.make(v, erro, Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.RED);
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();
            }
        });
    }

    private void salvarDadosUsuario() {
        String nome = editTextNome.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome);

        // Obtendo o token FCM
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    usuarios.put("deviceToken", token);

                    // Salvando o token no Firestore
                    String usuarioId = user.getUid();
                    DocumentReference documentReference = db.collection("Usuarios").document(usuarioId);

                    documentReference.set(usuarios)
                            .addOnSuccessListener(aVoid -> Log.d("sucess_db", "Sucesso ao salvar os dados"))
                            .addOnFailureListener(e -> Log.d("db_error", "Falha ao salvar os dados: " + e.toString()));
                    saveFCMToken(username);
                } else {
                    Log.w("FCM", "Failed to get FCM token", task.getException());
                }
            });
        }
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


    private void iniciarComponentes() {
        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        btnCadastrar = findViewById(R.id.buttonBtnCadastrar);
    }
}




