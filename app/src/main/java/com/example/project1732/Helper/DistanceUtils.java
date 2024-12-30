package com.example.project1732.Helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class DistanceUtils {

    // Método para calcular a distância entre dois locais
    public static double calculateDistance(LatLng userLocation, LatLng storeLocation) {
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                storeLocation.latitude,
                storeLocation.longitude,
                results
        );
        return results[0] / 1000.0; // Convertendo para quilômetros
    }

    // Método para calcular o preço com base na distância
    public static double calculatePrice(double distance) {
        if (distance <= 3.0) {
            return 5.00;
        } else if (distance <= 5.0) {
            return 7.00;
        } else {
            return 10.00;
        }
    }

    public static String calculateTime(double distance) {
        // Defina as velocidades médias
        final double DEFAULT_SPEED_KMH = 65.0; // Velocidade padrão para distâncias maiores que 10 km
        final double REDUCED_SPEED_KMH = 10;  // Velocidade reduzida para distâncias menores ou iguais a 1 km

        // Escolha a velocidade com base na distância
        double speed = distance <= 1 ? REDUCED_SPEED_KMH : DEFAULT_SPEED_KMH;

        // Calcular o tempo em horas
        double timeInHours = distance / speed;

        // Converter o tempo em horas para horas e minutos
        int hours = (int) timeInHours;
        int minutes = (int) ((timeInHours - hours) * 60);

        // Formatar o tempo em horas e minutos
        if (hours > 0) {
            // Se houver horas, formatar com horas e minutos
            return String.format("%d h %d min", hours, minutes);
        } else {
            // Se não houver horas, apenas mostrar minutos
            return String.format("%d min", minutes);
        }
    }
}

