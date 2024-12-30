package com.example.project1732.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project1732.Helper.ValidationUtils;
import com.example.project1732.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FormularioActivity extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextCpf, editTextTelefone, editTextEndereco, EditTexteEndereço;
    private EditText editTextRua, editTextNumero, editTextBairro, editTextcidade, editTextEstado;
    private Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        // Inicializando os
        editTextRua = findViewById(R.id.edtRua);
        editTextNumero = findViewById(R.id.edtNum);
        editTextBairro = findViewById(R.id.edtBairro);
        editTextcidade = findViewById(R.id.edtCidade);
        editTextEstado = findViewById(R.id.edtEstado);

        editTextNome = findViewById(R.id.etNome);
        editTextEmail = findViewById(R.id.etEmail);
        editTextCpf = findViewById(R.id.etCpf);
        editTextTelefone = findViewById(R.id.etTelefone);
        //editTextEndereco = findViewById(R.id.etEndereco);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Preenchendo os campos com informações do Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String nome = user.getDisplayName();
            String email = user.getEmail();

            // Criando o texto com prefixo
            String nomeText = "Nome: " + (nome != null ? nome : "");
            String emailText = "Email: " + (email != null ? email : "");

            // Usando Spannable para aplicar cor branca ao prefixo
            SpannableString nomeSpannable = new SpannableString(nomeText);
            SpannableString emailSpannable = new SpannableString(emailText);

            // Definindo a cor branca para o prefixo
            ForegroundColorSpan whiteSpan = new ForegroundColorSpan(Color.WHITE);

            // Aplicando o span ao texto (apenas para o prefixo)
            nomeSpannable.setSpan(whiteSpan, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // "Nome: " tem 6 caracteres
            emailSpannable.setSpan(whiteSpan, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // "Email: " tem 6 caracteres

            // Configurando o texto e o estilo
            editTextNome.setText(nomeSpannable);
            editTextEmail.setText(emailSpannable);
        }

        //Quando o botão "Salvar" for clicado
        btnSalvar.setOnClickListener(v -> {
            String editRua = "";
            String editBairro = "";
            String editNum = "";
            String editCidade = "";
            String editEstado = "";

            String cpf = editTextCpf.getText().toString().trim();
            String telefone = editTextTelefone.getText().toString().trim();

            // Obtenha os valores de cada EditText
            editRua = editTextRua.getText().toString().trim();
            editBairro = editTextBairro.getText().toString().trim();
            editNum = editTextNumero.getText().toString().trim();
            editCidade = editTextcidade.getText().toString().trim();
            editEstado = editTextEstado.getText().toString().trim();

            // Junte os componentes do endereço
            String endereco = editRua + " - " + editBairro + " - " + editNum + " - " + editCidade + " - " + editEstado;

            // Valida os campos
            if (validateFields(cpf, telefone, endereco)) {
                salvarInformacoes(cpf, telefone, endereco);
            }
        });

    }


    // Valida os campos de entrada
    private boolean validateFields(String cpf, String telefone, String endereco) {
        if (cpf.isEmpty() || !isValidCpf(cpf)) {
            Toast.makeText(FormularioActivity.this, "CPF inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

//        if (telefone.isEmpty() || !isValidTelefone(telefone)) {
//            Toast.makeText(FormularioActivity.this, "Telefone inválido", Toast.LENGTH_SHORT).show();
//            return false;
//        }

        if (endereco.isEmpty()) {
            Toast.makeText(FormularioActivity.this, "Endereço não pode ser vazio", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Método para validar CPF
    private boolean isValidCpf(String cpf) {
        return ValidationUtils.isValidCPF(cpf);
    }

    // Método para validar telefone (supondo que seja um número com 10 ou 11 dígitos)
//    private boolean isValidTelefone(String telefone) {
//        return telefone.length() >= 10 && telefone.length() <= 13;
//    }

    // Método para salvar informações no Firebase
    private void salvarInformacoes(String cpf, String telefone, String endereco) {
        // Obtenha o nome de usuário do Firebase Auth
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        // Referência ao nó "users" usando o nome de usuário
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("formulario");

        // Criar um mapa com os dados a serem atualizados
        Map<String, Object> updates = new HashMap<>();
        updates.put("cpf", cpf);
        updates.put("telefone", telefone);
        updates.put("endereco", endereco);

        // Salvando as informações no Firebase Realtime Database
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FormularioActivity.this, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show();
                // Voltar para a tela principal após salvar
                Intent intent = new Intent(FormularioActivity.this, Principal.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(FormularioActivity.this, "Erro ao salvar as informações", Toast.LENGTH_SHORT).show();
            }
        });
    }
}