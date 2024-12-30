package com.example.project1732.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project1732.R;

public class DialogTrocaSenha {
    public static void showTrocaSenhaDialog(
            Context context,
            String title,
            OnPositiveClickListener onPositiveClick,
            OnNegativeClickListener onNegativeClick
    ) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_trocar_senha, null);

        EditText senhaAntigaEditText = dialogView.findViewById(R.id.editTextSenhaAntiga);
        EditText novaSenhaEditText = dialogView.findViewById(R.id.editTextNovaSenha);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        Button btnAtualizar = dialogView.findViewById(R.id.btnAdicionar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            btnAtualizar.setOnClickListener(v -> {
                String senhaAntiga = senhaAntigaEditText.getText().toString();
                String novaSenha = novaSenhaEditText.getText().toString();

                if (!senhaAntiga.isEmpty() && !novaSenha.isEmpty()) {
                    onPositiveClick.onClick(senhaAntiga, novaSenha);
                    alertDialog.dismiss(); // Fecha o diálogo após o clique positivo
                } else {
                    Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            });

            btnCancelar.setOnClickListener(v -> {
                onNegativeClick.onClick();
                alertDialog.dismiss();
            });
        });

        // Configurar o título principal
        titleTextView.setText(title);

        alertDialog.show();
    }

    public interface OnPositiveClickListener {
        void onClick(String senhaAntiga, String novaSenha);
    }

    public interface OnNegativeClickListener {
        void onClick();
    }
}
