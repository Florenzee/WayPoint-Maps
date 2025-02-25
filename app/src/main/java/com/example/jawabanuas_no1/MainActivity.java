package com.example.jawabanuas_no1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_FIND_LOCATION = 99;
    public static int DEVAULT_UPDATE_INTERVAL() {
        return 30;
    }
    public static int FAST_UPDATE_INTERVAL() {
        return 5;
    }

    // References to the UI elements
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_update, tv_address, tv_countOfCrumbs;
    Switch sw_locationsupdates, sw_gps;
    Button btn_newWayPoint, btn_showWayPointList, btn_showMap;

    // Variable to remember if we are tracking location or not
    boolean updateOn = false;

    // Current location
    Location currentlocation;

    // List of saved location
    List<Location> savedLocations;

    // Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    // Google's API for location service (the majority of the app using class)
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Give each UI variable a value
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_update = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_countOfCrumbs = findViewById(R.id.tv_countOfCrumbs);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        btn_showMap = findViewById(R.id.btn_showMap);

        // Set all properties of LocationRequest
        locationRequest = new LocationRequest();

        // How often does the location check occur when set to the most frequent update
        locationRequest.setInterval(1000 * DEVAULT_UPDATE_INTERVAL());
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL());
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Event that is triggered whenver the update interval is met
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location
                updateUIValue(locationResult.getLastLocation());
            }
        };

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the GPS location

                // Add the new location to the global list
                MyApplication myApplication = (MyApplication)getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentlocation);
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    // most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                }
                else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsupdates.isChecked()) {
                    // Turn on location tracking
                    startLocationUpdates();
                }
                else {
                    // turn off location tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();
    } // End onCreate method

    private void startLocationUpdates() {
        tv_update.setText("Location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_update.setText("Location is NOT being tracked");
        tv_lat.setText("Location is NOT being tracked");
        tv_lon.setText("Location is NOT being tracked");
        tv_speed.setText("Location is NOT being tracked");
        tv_address.setText("Location is NOT being tracked");
        tv_accuracy.setText("Location is NOT being tracked");
        tv_altitude.setText("Location is NOT being tracked");
        tv_sensor.setText("Location is NOT being tracked");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FIND_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "This app requires permission to be granted properly", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void updateGPS() {
        // Get permissions from user to track GPS
        // Get the current location from the fused client
        // Update the UI

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // We got permission, put the value of location into the UI components
                    updateUIValue(location);
                    currentlocation = location;
                }
            });
        }
        else {
            // Permission not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FIND_LOCATION);
            }
        }
    }

    private void updateUIValue(Location location) {
        // Update all of the text view object with a new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        // Altitude
        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not available");
        }

        // Speed
        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else {
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e) {
            tv_address.setText("Unable to get street address");
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        // Show the number of waypoints saved
        tv_countOfCrumbs.setText(Integer.toString(savedLocations.size()));

    }


}