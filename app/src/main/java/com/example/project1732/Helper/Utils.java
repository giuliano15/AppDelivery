package com.example.project1732.Helper;

import android.widget.TextView;

import java.util.Arrays;

public class Utils {

    public static String formatCpf(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");

        // Verifica se o CPF tem 11 dígitos
        if (cpf.length() == 11) {
            // Aplica a máscara
            return cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9);
        } else {
            return "CPF inválido"; // Mensagem para CPF inválido
        }
    }

    public static String formatPhone(String phone) {
        // Remove caracteres não numéricos
        phone = phone.replaceAll("\\D", "");

        // Verifica se o telefone tem 10 ou 11 dígitos (com DDD e opcionalmente com 9 dígitos)
        if (phone.length() == 10) {
            return "(" + phone.substring(0, 2) + ") " +
                    phone.substring(2, 3) + " " +
                    phone.substring(3);
        } else if (phone.length() == 11) {
            return "(" + phone.substring(0, 2) + ") " +
                    phone.substring(2, 3) + " " +
                    phone.substring(3);
        } else {
            return "Telefone inválido"; // Mensagem para telefone inválido
        }
    }


    // Método para limitar o texto a no máximo três palavras e adicionar "..." se necessário
    public static String limitToThreeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text; // Retorna o texto original se vazio ou nulo
        }

        String[] words = text.split("\\s+"); // Divide o texto em palavras

        if (words.length <= 2) {
            return text; // Retorna o texto original se já tem 3 ou menos palavras
        } else {
            // Retorna apenas as primeiras 3 palavras com "..." no final
            return String.join(" ", Arrays.copyOfRange(words, 0, 2)) + "...";
        }
    }

    public static void setTextWithLimit(TextView textView, String text, int wordLimit) {
        String[] words = text.split("\\s+");
        if (words.length > wordLimit) {
            StringBuilder truncatedText = new StringBuilder();
            for (int i = 0; i < wordLimit; i++) {
                truncatedText.append(words[i]).append(" ");
            }
            truncatedText.append("...");
            textView.setText(truncatedText.toString().trim());
        } else {
            textView.setText(text);
        }
    }

}
