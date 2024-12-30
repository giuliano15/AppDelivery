package com.example.project1732.Helper;

import com.google.android.gms.maps.model.LatLng;

public interface LocationCallback {
    void onLocationRetrieved(LatLng userLocation, LatLng storeLocation, String userAddress);
}
