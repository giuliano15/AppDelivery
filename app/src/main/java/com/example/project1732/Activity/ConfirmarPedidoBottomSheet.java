package com.example.project1732.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.example.project1732.Domain.Foods;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.Helper.ProgressDialogUtil;
import com.example.project1732.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//public class ConfirmarPedidoBottomSheet extends BottomSheetDialogFragment {
//
//    private ManagmentCart managmentCart;
//
//    private TextView txtSubtotal, txtTotal, txtTaxaEntrega;
//    private EditText edtEndereco, edtObservacao;
//    private Button btnEnviarPedido;
//
//    private String endereco, telefone, userName, cpf, observacao;
//    private double taxaEntrega;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.activity_confirmar_pedido_bottom_sheet, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Inicializa os componentes
//        txtSubtotal = view.findViewById(R.id.txtSubtotal);
//        txtTaxaEntrega = view.findViewById(R.id.txtTaxaEntrega);
//        txtTotal = view.findViewById(R.id.txtTotal);
//        edtEndereco = view.findViewById(R.id.edtEndereco);
//        edtObservacao = view.findViewById(R.id.edtObservacao);
//        btnEnviarPedido = view.findViewById(R.id.btnEnviarPedido);
//
//        managmentCart = new ManagmentCart(getContext());
//
//        // Recupera os argumentos passados e utiliza o valor
//        Bundle args = getArguments();
//        if (args != null) {
//            double totalValue = args.getDouble("TOTAL_VALUE", 0.0);
//            taxaEntrega = args.getDouble("TAXA_ENTREGA", 0.0);
//            endereco = args.getString("DELIVERY_ADDRESS", "");
//            telefone = args.getString("TELEFONE", "");
//            userName = args.getString("USER_NAME", "");
//            cpf = args.getString("CPF", "");
//
//            BigDecimal subtotal = new BigDecimal(totalValue).setScale(2, RoundingMode.DOWN);
//            BigDecimal txtEntrega = new BigDecimal(taxaEntrega).setScale(2, RoundingMode.DOWN);
//
//            txtSubtotal.setText("R$" + subtotal);
//            txtTaxaEntrega.setText("R$" + txtEntrega);
//            edtEndereco.setText(endereco);
//            edtObservacao.setText(observacao);
//        }
//
//        btnEnviarPedido.setOnClickListener(v -> {
//            enviarPedido();
//        });
//
//        calcularTotal();
//    }
//
//    private void enviarPedido() {
//        // Captura a observação diretamente do EditText
//        observacao = getObservacao();
//        endereco = getEndereco();
//
//        if (txtTaxaEntrega != null) {
//            fetchPagSeguroSession();
//        } else {
//            Toast.makeText(getContext(), "Erro ao carregar o endereço ou CPF", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void fetchPagSeguroSession() {
//        String workerUrl = "https://my-first-worker.giulsilva.workers.dev/criar-checkout";
//
//        new Thread(() -> {
//            try {
//                // Configura a conexão
//                URL url = new URL(workerUrl);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setDoOutput(true); // Permite que você envie um corpo na requisição
//
//                // Cria o corpo da requisição JSON
//                JSONObject jsonBody = new JSONObject();
//                jsonBody.put("redirectUrl", "https://exemplo.com/pagamento-concluido");
//                JSONArray itemsArray = new JSONArray();
//
//                // Adicionando um exemplo de item. Você deve modificar isso com os dados reais dos itens do pedido.
//                JSONObject item = new JSONObject();
//                item.put("id", "1"); // ID do item
//                item.put("title", "Produto Exemplo"); // Nome do item
//                item.put("amount", "10.00"); // Preço do item
//                item.put("quantity", 1); // Quantidade do item
//                itemsArray.put(item);
//
//                jsonBody.put("items", itemsArray);
//
//                // Envia a requisição
//                try (OutputStream os = connection.getOutputStream()) {
//                    byte[] input = jsonBody.toString().getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }
//
//                // Verifica a resposta
//                int responseCode = connection.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    StringBuilder response = new StringBuilder();
//                    String inputLine;
//
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();
//
//                    JSONObject jsonResponse = new JSONObject(response.toString());
//                    String sessionId = jsonResponse.getString("sessionId");
//
//                    getActivity().runOnUiThread(() -> openPagSeguroCheckout(sessionId));
//                } else {
//                    Log.e("PagSeguroError", "Erro ao conectar ao Worker: " + responseCode);
//                    String errorResponse = new BufferedReader(new InputStreamReader(connection.getErrorStream())).lines().collect(Collectors.joining("\n"));
//                    Log.e("PagSeguroError", "Detalhes do erro: " + errorResponse);
//                    showToastOnUIThread("Erro ao conectar ao Worker: " + responseCode);
//                }
//            } catch (Exception e) {
//                Log.e("PagSeguroError", "Erro: " + e.getMessage());
//                showToastOnUIThread("Erro: " + e.getMessage());
//            }
//        }).start();
//    }
//
//
//
//
//
//    private void showToastOnUIThread(String message) {
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(() ->
//                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
//            );
//        }
//    }
//
//    private void openPagSeguroCheckout(String sessionId) {
//        WebView webView = new WebView(getActivity());
//        webView.setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//        getActivity().addContentView(webView, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//
//        webView.getSettings().setJavaScriptEnabled(true);
//        String checkoutUrl = "https://sandbox.pagseguro.uol.com.br/v2/checkout/payment.html?sessionId=" + sessionId;
//        webView.loadUrl(checkoutUrl);
//
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//
//                if (url.contains("transactionSuccess")) {  // Verifica se o pagamento foi bem-sucedido
//                    finalizarPedido();
//                } else if (url.contains("transactionFailed")) {
//                    Toast.makeText(getContext(), "Pagamento não aprovado. Tente novamente.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//
//    private void finalizarPedido() {
//        String paymentMethod = "Cartão de crédito";
//        String status = "Aprovado";
//
//        managmentCart.placeOrder(endereco, paymentMethod, cpf, telefone, userName, taxaEntrega, status, observacao);
//
//        ProgressDialogUtil.showProgressDialog(getActivity());
//
//        new Handler().postDelayed(() -> {
//            if (isAdded() && getActivity() != null) {
//                Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
//                startActivity(intent);
//                dismiss();
//                ProgressDialogUtil.hideProgressDialog(getActivity());
//                getActivity().finish();
//            }
//        }, 2000);
//    }
//
//    private void calcularTotal() {
//        String subtotalString = txtSubtotal.getText().toString().replace("R$", "").trim();
//        String taxaEntregaString = txtTaxaEntrega.getText().toString().replace("R$", "").trim();
//
//        subtotalString = subtotalString.replace(",", ".");
//        taxaEntregaString = taxaEntregaString.replace(",", ".");
//
//        double subtotalValue = Double.parseDouble(subtotalString);
//        double taxaEntregaValue = Double.parseDouble(taxaEntregaString);
//
//        double totalValue = subtotalValue + taxaEntregaValue;
//        txtTotal.setText("R$ " + String.format("%.2f", totalValue));
//    }
//
//    public String getEndereco() {
//        return edtEndereco.getText().toString();
//    }
//
//    public String getObservacao() {
//        return edtObservacao.getText().toString();
//    }
//}


public class ConfirmarPedidoBottomSheet extends BottomSheetDialogFragment {

    private ManagmentCart managmentCart;

    private TextView txtSubtotal, txtTotal, txtTaxaEntrega;
    private EditText edtEndereco, edtObservacao;

    private Button btnEnviarPedido;

    private String endereco, telefone, userName, cpf, observacao;

    private double taxaEntrega;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_confirmar_pedido_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa os componentes
        txtSubtotal = view.findViewById(R.id.txtSubtotal);
        txtTaxaEntrega = view.findViewById(R.id.txtTaxaEntrega);
        txtTotal = view.findViewById(R.id.txtTotal);
        edtEndereco = view.findViewById(R.id.edtEndereco);
        edtObservacao = view.findViewById(R.id.edtObservacao);
        btnEnviarPedido = view.findViewById(R.id.btnEnviarPedido);

        managmentCart = new ManagmentCart(getContext());


        // Recupera os argumentos passados e utiliza o valor
        Bundle args = getArguments();
        if (args != null) {
            double totalValue = args.getDouble("TOTAL_VALUE", 0.0);
            taxaEntrega = args.getDouble("TAXA_ENTREGA", 0.0);
            endereco = args.getString("DELIVERY_ADDRESS", "");
            telefone = args.getString("TELEFONE", "");
            userName = args.getString("USER_NAME", "");
            cpf = args.getString("CPF", "");

            BigDecimal subtotal = new BigDecimal(totalValue).setScale(2, RoundingMode.DOWN);
            BigDecimal txtEntrega = new BigDecimal(taxaEntrega).setScale(2, RoundingMode.DOWN);
            // Atualiza a TextView com o valor recebido
            txtSubtotal.setText("R$" + subtotal);
            txtTaxaEntrega.setText("R$" + txtEntrega);
            edtEndereco.setText(endereco);
            edtObservacao.setText(observacao);// Exibe o endereço recuperado
        }

        btnEnviarPedido.setOnClickListener(v -> {
            enviarPedido();
        });

        // Calcular e exibir o total somando os valores de subtotal e taxa de entrega
        calcularTotal();


    }

//    private void enviarPedido() {
//        // Obtendo o valor total e a descrição do pedido
//        double totalValue = Double.parseDouble(txtTotal.getText().toString().replace("R$", "").replace(",", ".").trim());
//        String descricao = "Pedido do usuário " + userName;
//
//        // Obtenha a lista de itens do carrinho
//        ArrayList<Foods> cartItems = managmentCart.getListCart();
//
//        // Criando um JSON Array de itens
//        JSONArray itemsArray = new JSONArray();
//        for (Foods item : cartItems) {
//            JSONObject itemJson = new JSONObject();
//            try {
//                itemJson.put("title", item.getTitle());
//                itemJson.put("quantity", item.getNumberInCart());
//                itemJson.put("unit_price", item.getPrice());
//                itemsArray.put(itemJson);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Criando o JSON do corpo da requisição
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("totalValue", totalValue);
//            jsonBody.put("descricao", descricao);
//            jsonBody.put("items", itemsArray);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        // Enviando a requisição para o Worker no Cloudflare
//        String url = "https://my-first-worker.giulsilva.workers.dev/createPreference";
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao enviar pedido", Toast.LENGTH_SHORT).show());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseData);
//                        String preferenceId = jsonObject.getString("preferenceId");
//
//                        String checkoutUrl = "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=" + preferenceId;
//
//                        PaymentStatusChecker paymentStatusChecker = new PaymentStatusChecker();
//                        paymentStatusChecker.startCheckingPaymentStatus(getContext(), preferenceId);
//
//                        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.color_primary))
//                                .setShowTitle(true)
//                                .build();
//                        customTabsIntent.intent.setPackage("com.android.chrome");
//
//                        customTabsIntent.launchUrl(requireContext(), Uri.parse(checkoutUrl));
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao processar resposta", Toast.LENGTH_SHORT).show());
//                    }
//                } else {
//                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro na resposta do servidor", Toast.LENGTH_SHORT).show());
//                }
//            }
//        });
//    }

//Esse codigo abaixo esta quase certo , mas pega apena nome de um produto
private void enviarPedido() {
    double totalValue = Double.parseDouble(txtTotal.getText().toString().replace("R$", "").replace(",", ".").trim());
    String descricao = "Pedido do usuário " + userName;

    ArrayList<Foods> cartItems = managmentCart.getListCart();

    // Recupera a taxa de entrega
    double taxaEntrega = Double.parseDouble(txtTaxaEntrega.getText().toString().replace("R$", "").replace(",", ".").trim());

    // Construa o JSON com os dados que serão enviados
    JSONObject json = new JSONObject();
    JSONArray jsonItems = new JSONArray();
    try {
        json.put("totalValue", totalValue);
        json.put("descricao", descricao);
        json.put("deliveryFee", taxaEntrega); // Adicione a taxa de entrega ao JSON principal

        // Adicione cada item à lista de itens
        for (Foods item : cartItems) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("id", item.getId());
            jsonItem.put("descricao", item.getTitle());
            jsonItem.put("quantidade", item.getNumberInCart());
            jsonItem.put("valorUnitario", item.getPrice());
            jsonItems.put(jsonItem);
        }

        // Adicione a lista de itens ao JSON principal
        json.put("items", jsonItems);
    } catch (JSONException e) {
        e.printStackTrace();
    }

    // URL do endpoint do seu Cloudflare Worker
    String url = "https://my-first-worker.giulsilva.workers.dev/createPreference";

    // Cria o request usando OkHttp
    OkHttpClient client = new OkHttpClient();
    RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
    Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

    // Executa a requisição de forma assíncrona
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao enviar pedido", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                // Processa a resposta
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String preferenceId = jsonObject.getString("preferenceId");

                    String status = "Aguarde...";
                    observacao = getObservacao();

                    managmentCart.placeOrder(endereco, "paymentMethod", cpf, telefone, userName, taxaEntrega, status, observacao);

                    // Inicie o checkout redirecionando para a URL do Mercado Pago
                    String checkoutUrl = "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=" + preferenceId;

                    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.color_primary))
                            .setShowTitle(true)
                            .build();

                    customTabsIntent.intent.setPackage("com.android.chrome");
                    customTabsIntent.launchUrl(requireContext(), Uri.parse(checkoutUrl));

                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Pedido enviado com sucesso!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao processar resposta", Toast.LENGTH_SHORT).show());
                }
            } else {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao enviar pedido", Toast.LENGTH_SHORT).show());
            }
        }
    });
}


//Estava usando este anteriormente , trocado pelo de cima dia 25/11/2024 --> motivo teste de lista no mercado pago

//    private void enviarPedido() {
//        double totalValue = Double.parseDouble(txtTotal.getText().toString().replace("R$", "").replace(",", ".").trim());
//        String descricao = "Pedido do usuário " + userName;
//
//        // Construa o JSON com os dados que serão enviados
//        String json = "{"
//                + "\"totalValue\":" + totalValue + ","
//                + "\"descricao\":\"" + descricao + "\""
//                + "}";
//
//        // URL do endpoint do seu Cloudflare Worker
//        String url = "https://my-first-worker.giulsilva.workers.dev/createPreference";
//
//        // Cria o request usando OkHttp
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//
//        // Executa a requisição de forma assíncrona
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao enviar pedido", Toast.LENGTH_SHORT).show());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseData);
//                        String preferenceId = jsonObject.getString("preferenceId");
//
//                        String status = "Aguarde...";
//                        observacao = getObservacao();
//
//                        managmentCart.placeOrder(endereco, "paymentMethod", cpf, telefone, userName, taxaEntrega, status, observacao);
//
//
//                        // Inicie o checkout redirecionando para a URL do Mercado Pago
//                        String checkoutUrl = "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=" + preferenceId;
//
//                        // Inicie a verificação do status do pagamento
//                        PaymentStatusChecker paymentStatusChecker = new PaymentStatusChecker();
//                        paymentStatusChecker.startCheckingPaymentStatus(getContext(),preferenceId); // Usando o preferenceId como paymentId
//
//                        // Configurando CustomTabsIntent
//                        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.color_primary)) // Cor da barra de ferramentas
//                                .setShowTitle(true) // Exibe o título da página
//                                .build();
//                        // Define o pacote do Chrome para Custom Tabs
//                        customTabsIntent.intent.setPackage("com.android.chrome");
//
//                        // Abrindo a URL no Custom Tab
//                        customTabsIntent.launchUrl(requireContext(), Uri.parse(checkoutUrl));
//
//
////                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
////                        startActivity(intent);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro ao processar resposta", Toast.LENGTH_SHORT).show());
//                    }
//                } else {
//                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro na resposta do servidor", Toast.LENGTH_SHORT).show());
//                }
//            }
//        });
//    }

    // Método de navegação após o sucesso da requisição
    private void startOrderHistoryActivity() {
        Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
        startActivity(intent);
        dismiss(); // Fecha o BottomSheet
        ProgressDialogUtil.hideProgressDialog(getActivity());
        getActivity().finish();
    }




    // Função para calcular o total (subtotal + taxa de entrega)
    private void calcularTotal() {
        // Extrair os valores de txtSubtotal e txtTaxaEntrega removendo o símbolo "R$"
        String subtotalString = txtSubtotal.getText().toString().replace("R$", "").trim();
        String taxaEntregaString = txtTaxaEntrega.getText().toString().replace("R$", "").trim();

        // Substituir a vírgula por ponto para converter corretamente para double
        subtotalString = subtotalString.replace(",", ".");
        taxaEntregaString = taxaEntregaString.replace(",", ".");

        // Converter os valores para Double
        double subtotalValue = Double.parseDouble(subtotalString);
        double taxaEntregaValue = Double.parseDouble(taxaEntregaString);

        // Somar os valores
        double totalValue = subtotalValue + taxaEntregaValue;

        // Exibir o valor total formatado no TextView txtTotal
        txtTotal.setText("R$ " + String.format("%.2f", totalValue));
    }

    // Função para obter os dados inseridos (pode ser utilizada externamente)
    public String getEndereco() {
        return edtEndereco.getText().toString();
    }

    public String getObservacao() {
        return edtObservacao.getText().toString();
    }
}
