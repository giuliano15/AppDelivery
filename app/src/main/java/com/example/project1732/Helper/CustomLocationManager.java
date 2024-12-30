package com.example.project1732.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CustomLocationManager {

    private static final String TAG = "CustomLocationManager";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION_BACKGROUND = 2;

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseReference;

    private final TextView cepTextView;
    private boolean useFirebaseLocation = false; // Nova flag para definir se vai usar localização do Firebase

    public CustomLocationManager(Activity activity, TextView cepTextView) {
        this.activity = activity;
        this.cepTextView = cepTextView;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Location"); // Caminho para o seu nó de localização padrão
    }

    // Nova função para trocar entre a localização do GPS e a localização vinda do Firebase
    public void setUseFirebaseLocation(boolean useFirebaseLocation) {
        this.useFirebaseLocation = useFirebaseLocation;
    }

    public interface LocationCallback {
        void onLocationRetrieved(LatLng userLocation, LatLng storeLocation, String userAddress);
    }

    public void retrieveLocation(final LocationCallback callback) {
        if (!isLocationEnabled()) {
            Toast.makeText(activity, "Por favor, ative a localização nas configurações do dispositivo.", Toast.LENGTH_LONG).show();
            return;
        }

        // Verifica permissões de localização
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (activity instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                // Trate o erro ou avise que não é possível requisitar permissões
                Log.e(TAG, "Contexto não é uma instância de Activity. Não é possível requisitar permissões.");
                Toast.makeText(activity, "Erro: o contexto não é uma Activity.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (activity instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) activity,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_LOCATION_PERMISSION_BACKGROUND);
            } else {
                // Trate o erro ou avise que não é possível requisitar permissões
                Log.e(TAG, "Contexto não é uma instância de Activity. Não é possível requisitar permissões.");
                Toast.makeText(activity, "Erro: o contexto não é uma Activity.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

//        // Verifica permissões de localização
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION_PERMISSION);
//            return;
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
//                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context,
//                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                    REQUEST_LOCATION_PERMISSION_BACKGROUND);
//            return;
//        }

        // Decide se vai buscar a localização do Firebase ou usar a localização via GPS
        if (useFirebaseLocation) {
            getUserLocationFromFirebase(callback);
        } else {
            // Tenta obter a localização com FusedLocationProviderClient
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            String userCep = getAddressFromLatLng(userLocation);
                            // Defina o endereço no TextView
                            if (userCep != null) {
                                cepTextView.setText(userCep);
                            } else {
                                cepTextView.setText("Endereço não encontrado");
                            }

                            // Obtenha a localização da loja e trate a resposta
                            getStoreLocationFromFirebase(storeLocation -> {
                                if (storeLocation != null) {
                                    LatLng storeLatLng = getLatLngFromAddress(storeLocation);
                                    String userAddress = getAddressFromLatLng(userLocation);
                                    callback.onLocationRetrieved(userLocation, storeLatLng, userAddress);
                                } else {
                                    Toast.makeText(activity, "Não foi possível obter a localização da loja.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "Localização do usuário é nula, tentando com LocationManager...");
                            // Alternativa com LocationManager
                            getLocationWithLocationManager(callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha ao obter a localização com FusedLocationProviderClient", e);
                        Toast.makeText(activity, "Falha ao obter a localização: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para buscar a localização do usuário diretamente do Firebase
    private void getUserLocationFromFirebase(final LocationCallback callback) {
        String userName = UserManager.getInstance(activity).getUserName();
        DatabaseReference userLocationRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userName)
                .child("formulario")
                .child("endereco");

        userLocationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String address = task.getResult().getValue(String.class);
                if (address != null) {
                    LatLng userLocation = getLatLngFromAddress(address);

                    String userCep = getAddressFromLatLng(userLocation);
                    if (userCep != null) {
                        cepTextView.setText(userCep);
                    } else {
                        cepTextView.setText("Endereço não encontrado");
                    }

                    if (userLocation != null) {
                        getStoreLocationFromFirebase(storeLocation -> {
                            if (storeLocation != null) {
                                LatLng storeLatLng = getLatLngFromAddress(storeLocation);
                                String userAddress = getAddressFromLatLng(userLocation);
                                callback.onLocationRetrieved(userLocation, storeLatLng, userAddress);
                            } else {
                                Toast.makeText(activity, "Não foi possível obter a localização da loja.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    //Toast.makeText(context, "Endereço não encontrado no Firebase, buscando do dispositivo.", Toast.LENGTH_SHORT).show();
                    retrieveLocationWithGPS(callback);
                }
            } else {
                Toast.makeText(activity, "Erro ao buscar a localização do Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocationWithLocationManager(final LocationCallback callback) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                String userCep = getAddressFromLatLng(userLocation);
                if (userCep != null) {
                    cepTextView.setText(userCep);
                } else {
                    cepTextView.setText("Endereço não encontrado");
                }

                // Obtenha a localização da loja e trate a resposta
                getStoreLocationFromFirebase(storeLocation -> {
                    if (storeLocation != null) {
                        LatLng storeLatLng = getLatLngFromAddress(storeLocation);
                        String userAddress = getAddressFromLatLng(userLocation);
                        callback.onLocationRetrieved(userLocation, storeLatLng, userAddress);
                    } else {
                        Toast.makeText(activity, "Não foi possível obter a localização da loja.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        });
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return gpsEnabled || networkEnabled;
    }

    private void getStoreLocationFromFirebase(OnStoreLocationRetrievedCallback callback) {
        databaseReference.child("0").child("loc").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String storeAddress = task.getResult().getValue(String.class);
                callback.onStoreLocationRetrieved(storeAddress);
            } else {
                callback.onStoreLocationRetrieved(null);
            }
        });
    }

    private LatLng getLatLngFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        int retryCount = 3; // Número de tentativas com o Geocoder
        int currentAttempt = 0;

        while (currentAttempt < retryCount) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    return new LatLng(location.getLatitude(), location.getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
                currentAttempt++;
            }
        }
        return null;
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // Retorna o endereço completo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private interface OnStoreLocationRetrievedCallback {
        void onStoreLocationRetrieved(String storeLocation);
    }

    private void retrieveLocationWithGPS(final LocationCallback callback) {
        // Reutilize o método já implementado para buscar localização via GPS
        retrieveLocation(callback);
    }
}

