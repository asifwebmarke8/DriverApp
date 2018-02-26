package com.webmarke8.app.gencartdriver.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.webmarke8.app.gencartdriver.R;

public class OnRide extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_ride);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        LatLng UCA = new LatLng(-34, 151);
//        map.addMarker(new MarkerOptions().position(UCA).title("YOUR TITLE")).showInfoWindow();
//
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(UCA, 17));

    }
}
