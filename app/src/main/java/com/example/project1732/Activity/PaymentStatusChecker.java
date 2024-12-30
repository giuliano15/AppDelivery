package com.example.project1732.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PaymentStatusChecker {

    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final String FIREBASE_URL = "https://projetofood173-default-rtdb.firebaseio.com/payments/";

    private Context context;

    // Variável para armazenar o paymentId
    private String paymentId;

    // Runnable para verificar o status periodicamente
    private final Runnable statusCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkPaymentStatus(paymentId);
            handler.postDelayed(this, 5000); // Repetir a cada 5 segundos
        }
    };

    // Método para iniciar a verificação de status com o paymentId
    public void startCheckingPaymentStatus(Context context,String paymentId) {
        this.context = context;
        this.paymentId = paymentId; // Armazena o paymentId na variável da classe
        handler.post(statusCheckRunnable); // Inicia o Runnable
    }

    // Método que verifica o status do pagamento
    private void checkPaymentStatus(String paymentId) {
        String url = FIREBASE_URL + paymentId + ".json";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String paymentStatus = jsonResponse.optString("status", "");

                        if ("approved".equals(paymentStatus)) {
                            handler.removeCallbacks(statusCheckRunnable); // Para a verificação
                           // openOrderHistory(); // Redireciona para o histórico de pedidos
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Método para abrir o histórico de pedidos
    private void openOrderHistory() {
        Intent intent = new Intent(context, OrderHistoryActivity.class);
        context.startActivity(intent);
        Toast.makeText(context, "Pagamento aprovado teste!", Toast.LENGTH_SHORT).show();
    }
}


//public class PaymentStatusChecker extends  {
//
//
//
//    private final OkHttpClient client = new OkHttpClient();
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private static final String FIREBASE_URL = "https://projetofood173.firebaseio.com/payments/";
//
//    private final Runnable statusCheckRunnable = new Runnable() {
//        @Override
//        public void run() {
//            checkPaymentStatus();
//            handler.postDelayed(this, 5000); // Repetir a cada 5 segundos
//        }
//    };
//
//    public void startCheckingPaymentStatus(String paymentId) {
//        handler.post(statusCheckRunnable);
//    }
//
//    private void checkPaymentStatus(String paymentId) {
//        String url = FIREBASE_URL + paymentId + ".json";
//        Request request = new Request.Builder().url(url).build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    try {
//                        JSONObject jsonResponse = new JSONObject(responseData);
//                        String paymentStatus = jsonResponse.optString("status", "");
//
//                        if ("approved".equals(paymentStatus)) {
//                            handler.removeCallbacks(statusCheckRunnable);
//                            openOrderHistory();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    private void openOrderHistory() {
//        Intent intent = new Intent(context, OrderHistoryActivity.class);
//        context.startActivity(intent);
//        Toast.makeText(context, "Pagamento aprovado!", Toast.LENGTH_SHORT).show();
//    }
//}
