package com.example.fypapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import javax.mail.internet.AddressException;

public class LiveLocation extends FragmentActivity implements OnMapReadyCallback {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node, latnode, longnode;
    private double latt = 0, longg = 0;
    FrameLayout map;
    GoogleMap gMap;
    Location currentLocation;
    Marker marker;
    FusedLocationProviderClient fusedClient;
    private static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        node = db.getReference("Location");
        latnode = node.child("Lattitude");
        longnode = node.child("Longitude");

        latnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latt = (double) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LiveLocation.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        longnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                longg = (double) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LiveLocation.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geo = new Geocoder(LiveLocation.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latt, longg, 1);
                    if (addresses.isEmpty()) {
                        node.child("address").setValue("0");
                    } else {
                        if (addresses.size() > 0) {
                            node.child("address").setValue(addresses.get(0).getAddressLine(0));
                        }
                    }
                    map = findViewById(R.id.map);
                    fusedClient = LocationServices.getFusedLocationProviderClient(LiveLocation.this);
                    getLocation();
                } catch (Exception e) {
                    Toast.makeText(LiveLocation.this, "Connection Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                handler.postDelayed(this, 1000);
            }
        }, 3000); //the time you want to delay in milliseconds
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        try {
            Task<Location> task = fusedClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        try {
                            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            supportMapFragment.getMapAsync(LiveLocation.this);
                        } catch (Exception e) {

                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(LiveLocation.this, "Connection Error" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        googleMap.clear();

        LatLng latLng = new LatLng(latt, longg);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Rider").icon(BitmapDescriptorFactory.defaultMarker());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }


    //    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent i = new Intent(LiveLocation.this, GuardianDashboard.class);
        startActivity(i);
//        moveTaskToBack();
    }
}