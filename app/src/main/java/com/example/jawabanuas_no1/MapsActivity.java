package com.example.jawabanuas_no1;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.jawabanuas_no1.databinding.ActivityMapsBinding;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Location> savedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Find the button and set an OnClickListener
        Button btnRecenter = findViewById(R.id.btn_recenter);
        btnRecenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if mMap is ready
                if (mMap != null) {
                    // Center the map to Braga
                    LatLng braga = new LatLng(-6.9178283, 107.6045685);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(braga, 12.0f));
                } else {
                    Toast.makeText(MapsActivity.this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Retrieve saved locations from MyApplication
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Menambahkan marker untuk Kampus Binus Bandung
        LatLng binusBandung = new LatLng(-6.9153653, 107.5886954);
        mMap.addMarker(new MarkerOptions().position(binusBandung).title("Kampus Binus Bandung"));

        // Menambahkan marker untuk Braga
        LatLng braga = new LatLng(-6.9178283, 107.6045685);
        mMap.addMarker(new MarkerOptions().position(braga).title("Braga"));

        // Menambahkan marker untuk Alun-Alun Kota Bandung
        LatLng alunAlunBandung = new LatLng(-6.9218295, 107.6021967);
        mMap.addMarker(new MarkerOptions().position(alunAlunBandung).title("Alun-Alun Kota Bandung"));

        // Menambahkan marker untuk Lapangan Gazibu
        LatLng lapanganGazibu = new LatLng(-6.9002779, 107.6161296);
        mMap.addMarker(new MarkerOptions().position(lapanganGazibu).title("Lapangan Gazibu"));

        // Memindahkan kamera ke salah satu lokasi (misalnya, Braga)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(braga, 12.0f));

        // Add saved locations as markers on the map
        if (savedLocations != null && !savedLocations.isEmpty()) {
            LatLng lastLocationPlaced = null;
            for (Location location : savedLocations) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
                mMap.addMarker(markerOptions);
                lastLocationPlaced = latLng;
            }

            if (lastLocationPlaced != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationPlaced, 12.0f));
            }
        }

        // Menambahkan listener untuk klik pada marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                // Menghitung jumlah klik pada pin
                Integer clicks = (Integer) marker.getTag();
                if (clicks == null) {
                    clicks = 0;
                }
                clicks++;
                marker.setTag(clicks);
                Toast.makeText(MapsActivity.this, "Marker " + marker.getTitle() + " was clicked " + marker.getTag() + " times.", Toast.LENGTH_SHORT).show();

                return false;
            }
        });
    }
}
