package com.example.project1732.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1732.Adapter.BestFoodAdapter;
import com.example.project1732.Adapter.CategoryAdapter;
import com.example.project1732.Domain.Category;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Domain.Location;
import com.example.project1732.Domain.Price;
import com.example.project1732.Domain.Time;
import com.example.project1732.Helper.CustomLocationManager;
import com.example.project1732.Helper.DistanceUtils;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.Helper.PermissionUtils;
import com.example.project1732.Helper.TaxaDelivery;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.R;
import com.example.project1732.apiMaps.DirectionsApi;
import com.example.project1732.apiMaps.DirectionsResponse;
import com.example.project1732.apiMaps.RetrofitClient;
import com.example.project1732.databinding.ActivityMainBinding;
import com.example.project1732.via_cep.presenter.MainActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Principal extends BaseActivity {

    private CustomLocationManager customLocationManager;
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final int REQUEST_LOCATION_PERMISSION_BACKGROUND = 3;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private ActivityMainBinding binding;
    private boolean doubleBackToExitPressedOnce = false;
    private String userName;
    private UserManager userManager;
    private double globalPrice = 0.0;

    private TextView txtCep;

    private ManagmentCart managmentCart;

    private static final int REQUEST_CODE_OPEN_MAPS = 4;

    //Do telegram
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent it = getIntent();
        String action = it.getAction();
        Uri data = it.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String host = data.getHost();
            if ("success".equals(host)) {
                // Lógica para sucesso
               // Toast.makeText(this, "Pagamento aprovado!", Toast.LENGTH_SHORT).show();
                openOrderHistory();
            } else if ("failure".equals(host)) {
                // Lógica para falha
                Toast.makeText(this, "Pagamento falhou!", Toast.LENGTH_SHORT).show();
            } else if ("pending".equals(host)) {
                // Lógica para pendente
                Toast.makeText(this, "Pagamento pendente!", Toast.LENGTH_SHORT).show();
            }
        }

        // Inicializa o UserManager
        managmentCart = new ManagmentCart(this);

        binding.favorite.setOnClickListener(v -> {
            Intent intent = new Intent(this, Favoritos.class);
            startActivity(intent);
        });

        recuperaEndereco();

        customLocationManager = new CustomLocationManager(this, txtCep);

        customLocationManager.retrieveLocation((userLocation, storeLocation, userAddress) -> {
            // Aqui você pode tratar os dados da localização recuperada
        });

        getUserName();
        initLocation();
        initTime();
        initPrice();
        initBestFood();
        initCategory();
        setVariable();
        getHistoryPlace();

        checkAndRequestPermissions();

        // Verifica se as informações do usuário estão completas
        checkUserInformation();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifica se as permissões de localização foram concedidas
        if (PermissionUtils.hasLocationPermissions(this)) {
            // Se a permissão já foi concedida, inicia o processo de localização
            fetchLocations();
        }
    }
    private void openOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        this.startActivity(intent);
        Toast.makeText(this, "Verifique o status do pedido no Histórico de pedidos.!", Toast.LENGTH_LONG).show();
    }

    private void checkUserInformation() {
        // Obtém o nome de usuário do Firebase Auth
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        // Verifica se o nome de usuário está disponível
        if (username == null) {
            Toast.makeText(Principal.this, "Nome de usuário não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referência ao nó "formulario" do usuário específico
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("formulario");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Verifica se o nó "formulario" existe para o usuário
                if (dataSnapshot.exists()) {
                    // Verifica se os campos obrigatórios estão preenchidos
                    boolean hasCpf = dataSnapshot.child("cpf").exists();
                    boolean hasTelefone = dataSnapshot.child("telefone").exists();
                    boolean hasEndereco = dataSnapshot.child("endereco").exists();

                    // Se algum campo estiver faltando, redireciona para a tela de formulário
                    if (!hasCpf || !hasTelefone || !hasEndereco) {
                        Intent intent = new Intent(Principal.this, FormularioActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Se todos os campos estão preenchidos, continua normalmente
                       // Toast.makeText(Principal.this, "Informações completas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Se o nó "formulario" não existir para o usuário
                    Intent intent = new Intent(Principal.this, FormularioActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Trata erros de conexão ou leitura do Firebase
                Toast.makeText(Principal.this, "Erro ao acessar os dados do usuário", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recuperaEndereco() {

        txtCep = (binding.txtCep);
        binding.btnEnd.setOnClickListener(v -> {
            //chama activity Endereços
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        binding.txtCep.setOnClickListener(v -> {
            //chama activity Endereços
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

//Erro foi encontrado aqui, para versão nova usa-se getApplicationContext() e versão antiga usa se activity(this)

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0 (API nível 23) e superior
            // Verificar permissões de localização
            //Erro foi encontrado aqui, para versão nova usa-se getApplicationContext() e versão antiga usa se activity(this)
            if (PermissionUtils.hasLocationPermissions(getApplicationContext())) {
                fetchLocations();
            } else {
                // Solicitar permissões de localização
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_LOCATION_PERMISSION);
            }
        } else {
            // Para versões abaixo do Android 6.0
            fetchLocations();
        }
        // Verificar permissão de notificação no Android 13 e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.hasNotificationPermission(getApplicationContext())) {
                // Solicitar permissão de notificação
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.POST_NOTIFICATIONS
                }, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

    }

    // Método de retorno para verificar se as permissões foram concedidas ou negadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Verifica se a permissão de localização foi concedida
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocations(); // Se a permissão foi concedida, continua o fluxo
            } else {
                Toast.makeText(this, "Permissão de localização necessária para continuar.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_LOCATION_PERMISSION_BACKGROUND) {
            // Verifica se a permissão de localização em segundo plano foi concedida
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocations();
            } else {
                Toast.makeText(this, "Permissão de localização em segundo plano necessária.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchLocations() {

        // Decida se quer usar a localização do Firebase ou do GPS
        boolean usarFirebase = true;  // Defina como `false` se quiser usar o GPS
        customLocationManager.setUseFirebaseLocation(usarFirebase);

        // Chame o método retrieveLocation passando o callback
        customLocationManager.retrieveLocation((userLocation, storeLocation, userAddress) -> {
            if (storeLocation != null && userLocation != null) {
                // Calcule a distância entre o usuário e a loja
                double distance = DistanceUtils.calculateDistance(userLocation, storeLocation);
                globalPrice = DistanceUtils.calculatePrice(distance); // Calcula o preço da entrega com base na distância
                String time = DistanceUtils.calculateTime(distance);  // Calcula o tempo estimado com base na distância

                // Define o preço de entrega global usando um singleton (TaxaDelivery)
                TaxaDelivery.getInstance().setGlobalPrice(globalPrice);

                //insira sua api do Google Maps
                String apiKey = getString(R.string.maps_api_key);

                // Se quiser usar um método externo para calcular o tempo de viagem, descomente a linha abaixo e adicione sua chave de API do Google:
                 getTravelTime(apiKey, userLocation, storeLocation);

                // Atualiza a UI com as informações de distância, preço e tempo estimado
                updateUI(distance, globalPrice, time);
            } else {
                // Caso não tenha conseguido obter as localizações
                Toast.makeText(Principal.this, "Não foi possível determinar as localizações.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(double distance, double price, String time) {
        binding.locationSp.setText("Local");
        binding.priceSp.setText(String.format(Locale.getDefault(), "R$ %.2f", price));
        binding.timeSp.setText("carregando...");
    }

    private void getHistoryPlace() {
        binding.btnPedidosFeitos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void getUserName() {
        userManager = UserManager.getInstance(this);
        userManager.refreshUser();
        userName = userManager.getUserName();
        binding.txtNameUser.setText(userName);
    }

    private void setVariable() {
        binding.TxtVerTds.setOnClickListener(v -> startActivity(new Intent(Principal.this, ListLoadAllActivity.class)));

        binding.cartBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Principal.this, CartActivity.class);
            intent.putExtra("EXTRA_PRICE", TaxaDelivery.getInstance().getGlobalPrice());
            startActivity(intent);
        });

        binding.imgSair.setOnClickListener(v -> deslogarUsuario());

        binding.searchBtn.setOnClickListener(v -> {
            String text = binding.searchEdt.getText().toString();
            if (!text.isEmpty()) {
                Intent intent = new Intent(Principal.this, ListFoodActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("isSearch", true);
                startActivity(intent);
                binding.searchEdt.setText("");
            }
        });

        binding.locationSp.setOnClickListener(v -> {
            if (customLocationManager != null) {
                customLocationManager.retrieveLocation((userLocation, storeLocation, userAddress) -> {
                    if (storeLocation != null) {
                        fetchLocationNameAndOpenMaps(storeLocation);
                    } else {
                        Toast.makeText(Principal.this, "Localização da loja não definida", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //Metodo para abrir Google maps com localização
    private void openGoogleMaps(LatLng storeLocation, String nameLocation) {
        // Cria a URL para o Google Maps com a localização da loja
        String uri = "geo:" + storeLocation.latitude + "," + storeLocation.longitude + "?q=" + storeLocation.latitude + "," + storeLocation.longitude + "(" + Uri.encode(nameLocation) + ")";

        // Cria o Intent para abrir o Google Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        // Verifica se o Google Maps está instalado
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Google Maps não está instalado, trate o caso aqui se necessário
            Toast.makeText(this, "Google Maps não está instalado", Toast.LENGTH_SHORT).show();
        }
    }


    //Metodo para recuperar nome do local endereço no firebase
    private void fetchLocationNameAndOpenMaps(LatLng storeLocation) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Location").child("0");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nameLocation = dataSnapshot.child("nameLocation").getValue(String.class);
                if (nameLocation != null) {
                    openGoogleMaps(storeLocation, nameLocation);
                } else {
                    Toast.makeText(Principal.this, "Nome da localização não encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(Principal.this, "Erro ao acessar o Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Este metodo abaixo ainda não esta funcionando erro chave api GOOGLE Maps
    public void getTravelTime(String apiKey, LatLng origin, LatLng destination) {
        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;

        DirectionsApi directionsApi = RetrofitClient.getInstance().create(DirectionsApi.class);
        directionsApi.getDirections(originStr, destinationStr, apiKey)
                .enqueue(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.isSuccessful()) {
                            DirectionsResponse directions = response.body();
                            if (directions != null && directions.getRoutes().size() > 0) {
                                DirectionsResponse.Route route = directions.getRoutes().get(0);
                                DirectionsResponse.Route.Leg leg = route.getLegs().get(0);
                                DirectionsResponse.Duration duration = leg.getDuration();

                                if (duration != null) {
                                    // Exibir a duração estimada ao usuário
                                    Toast.makeText(Principal.this, "Tempo estimado: " + duration.getText(), Toast.LENGTH_LONG).show();

                                    binding.timeSp.setText(duration.getText());

                                }
                            }
                        } else {
                            Toast.makeText(Principal.this, "Resposta não foi bem-sucedida", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast.makeText(Principal.this, "Erro ao obter o tempo de viagem", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_MAPS) {
            // Aqui você pode tratar o retorno do Google Maps, se necessário
        }
    }

    private void initCategory() {

        DatabaseReference myref = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issu : snapshot.getChildren()) {
                        list.add(issu.getValue(Category.class));

                    }
                    if (list.size() > 0) {
                        binding.categoryView.setLayoutManager(new GridLayoutManager(Principal.this, 4));
                        RecyclerView.Adapter adapterCategory = new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapterCategory);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }


    private void initBestFood() {
        DatabaseReference myref = database.getReference("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();
        Query query = myref.orderByChild("BestFood").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issu : snapshot.getChildren()) {
                        list.add(issu.getValue(Foods.class));
                    }
                    if (list.size() > 0) {
                        binding.bestFoodView.setLayoutManager(new LinearLayoutManager(Principal.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter adapterBestFood = new BestFoodAdapter(list);
                        binding.bestFoodView.setAdapter(adapterBestFood);
                    }
                    binding.progressBarBestFood.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    private void initLocation() {
        DatabaseReference myRef = database.getReference("Location");
        ArrayList<Location> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {

                        list.add(issue.getValue(Location.class));
                    }
                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(Principal.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // binding.locationSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initTime() {
        DatabaseReference myRef = database.getReference("Time");
        ArrayList<Time> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        list.add(issue.getValue(Time.class));
                    }
                    ArrayAdapter<Time> adapter = new ArrayAdapter<>(Principal.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // binding.timeSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initPrice() {
        DatabaseReference myRef = database.getReference("Price");
        ArrayList<Price> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        list.add(issue.getValue(Price.class));
                    }
                    ArrayAdapter<Price> adapter = new ArrayAdapter<>(Principal.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //  binding.priceSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Principal.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Chama a função para fechar o aplicativo somente quando doubleBackToExitPressedOnce é true
            finishApplication();
        } else {
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Pressione novamente para sair", Toast.LENGTH_SHORT).show();

            // Define um temporizador para redefinir doubleBackToExitPressedOnce após um período de 2 segundos
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    private void finishApplication() {
        // Finaliza a atividade principal e fecha o aplicativo
        finishAffinity();
    }

    private void deslogarUsuario() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Você realmente deseja sair?")
                .setPositiveButton("Sim", (dialog, which) -> {

                    if (managmentCart != null) {
                        managmentCart.clearLocalData();
                    } else {
                        Log.e("Principal", "ManagmentCart está null!");
                    }
                    FirebaseAuth.getInstance().signOut();
                    finishApplication();
                    Intent intent = new Intent(Principal.this, LoginActivity.class);
                    startActivity(intent);
                    //finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

//    public void testApiKey(String apiKey) {
//        // Defina a URL base para o Retrofit
//        String baseUrl = "https://maps.googleapis.com/";
//
//        // Crie uma instância do Retrofit
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Crie uma instância da interface DirectionsApi
//        DirectionsApi directionsApi = retrofit.create(DirectionsApi.class);
//
//        // Defina o URL de teste para a API
//        String originPlaceId = "place_id:ChIJMyzPysqQpgARlznSOl55NVs";
//        String destinationPlaceId = "place_id:ChIJFVR2MkcHpQARmru5TDcVVuc";
//
//        // Faça a chamada para a API do Google Directions
//        directionsApi.getDirections(originPlaceId, destinationPlaceId, apiKey)
//                .enqueue(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                        if (response.isSuccessful()) {
//                            // A chave da API está funcionando
//                            Toast.makeText(MainActivity.this, "Chave da API está funcionando", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // A chave da API não está funcionando
//                            Toast.makeText(MainActivity.this, "Chave da API não está funcionando. Código de resposta: " + response.code(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//                        // Falha ao testar a chave da API
//                        Toast.makeText(MainActivity.this, "Falha ao testar a chave da API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


//    public void testApiKey(String apiKey) {
//        String testUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=place_id:ChIJN1t_tDeuEmsRUsoyG83frY4&destination=place_id:ChIJE9o5HHlzEmsRZOwE3e7PZ5E&key=" + apiKey;
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        DirectionsApi directionsApi = retrofit.create(DirectionsApi.class);
//
//        directionsApi.getDirections("place_id:ChIJN1t_tDeuEmsRUsoyG83frY4", "place_id:ChIJE9o5HHlzEmsRZOwE3e7PZ5E", apiKey)
//                .enqueue(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                        if (response.isSuccessful()) {
//                            Toast.makeText(MainActivity.this, "Chave da API está funcionando", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(MainActivity.this, "Chave da API não está funcionando. Código de resposta: " + response.code(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//                        Toast.makeText(MainActivity.this, "Falha ao testar a chave da API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

}
