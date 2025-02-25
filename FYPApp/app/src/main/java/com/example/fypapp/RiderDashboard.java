package com.example.fypapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderDashboard extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node, oilChangenode, kmtodrivenode, batterynode, weathernode, swnode, sosnode, bikenode, helmetnode, s4node, node2,
            speednode, speedlimitnode, node3, node4, node5, latnode, longnode;
    TextView health, weather;
    private ActivityResultLauncher<String> pushNotificationPermissionLauncher;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private int btState = 0, btState2 = 0, btState3 = 0;
    String getName = " ";
    float speedlimit = 0;
    private float SaveKmtodrive = 0;
    double latt = 0, longg = 0;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_dashboard);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("RiderPref", 0); // 0 - for private mode
        editor = pref.edit();

        //Get text from Intent
        getName = pref.getString("Email", "");
        Log.e("MainActivity - Email: ", getName);

        CardView EmergencySOS = findViewById(R.id.cardview2);
        CardView BikeBtn = findViewById(R.id.cardview1);
        CardView KmDriven = findViewById(R.id.cardview4);
        CardView LLocation = findViewById(R.id.cardview3);
        CardView AreaRisk = findViewById(R.id.cardview6);
        CardView OilChange = findViewById(R.id.cardview5);
        health = (TextView) findViewById(R.id.textView17);
        weather = (TextView) findViewById(R.id.textView20);
        TextView biketext = (TextView) findViewById(R.id.textView3);

        EmergencySOS.getBackground().setTint(Color.WHITE);
        BikeBtn.getBackground().setTint(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notificcation ID", "Notificcation ID", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        node5 = db.getReference("Location");
        latnode = node5.child("Lattitude");
        longnode = node5.child("Longitude");
        node = db.getReference("Buttons");
        node4 = db.getReference("OilChange");
        oilChangenode = node4.child("KmDrivenOil");
        kmtodrivenode = node4.child("KmToDrive");
        batterynode = node.child("batteryHealth");
        weathernode = node.child("weather");
        sosnode = node.child("SOSBtn");
        bikenode = node.child("BikeOffBtn");


        latnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                latt = Double.parseDouble(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        longnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                longg = Double.parseDouble(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        kmtodrivenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    SaveKmtodrive = Float.parseFloat(value);
                } catch (NumberFormatException nfe) {
                    Toast.makeText(RiderDashboard.this, "Integer Parse Error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        oilChangenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    float num = Float.parseFloat(value);
                    if (num > SaveKmtodrive) {
                        addNotificationOil();
                    }
                } catch (NumberFormatException nfe) {
                    Toast.makeText(RiderDashboard.this, "Oil Change Notify Error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        batterynode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                health.setText(value + "%");
                try {
                    int num = Integer.parseInt(value);
                    if (num < 20) {
                        addNotificationBattery();
                    }
                } catch (NumberFormatException nfe) {
//                    Toast.makeText(RiderDashboard.this, "Connection Error 5", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        weathernode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                weather.setText(value + "°");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        sosnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    EmergencySOS.getBackground().setTint(Color.GREEN);
                    btState3 = 1;
                    addNotificationEmergency();
                } else if (value.equals("1")) {
                    EmergencySOS.getBackground().setTint(Color.WHITE);
                    btState3 = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        bikenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("0")) {
                    biketext.setText("Bike ON");
                    BikeBtn.getBackground().setTint(Color.GREEN);
                    btState = 1;
                    addNotificationBike();
                } else if (value.equals("1")) {
                    biketext.setText("Bike OFF");
                    BikeBtn.getBackground().setTint(Color.WHITE);
                    btState = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderDashboard.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });


        EmergencySOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btState3 == 0) {
                    // Writing data to Firebase
                    node.child("SOSBtn").setValue("0");
                    EmergencySOS.getBackground().setTint(Color.GREEN);
                    btState3 = 1;
                    Toast.makeText(RiderDashboard.this, "Message Sent !", Toast.LENGTH_LONG).show();
//                    addNotificationEmergency();
                } else {
                    // Writing data to Firebase
                    node.child("SOSBtn").setValue("1");
                    EmergencySOS.getBackground().setTint(Color.WHITE);
                    btState3 = 0;
//                    Toast.makeText(RiderDashboard.this, "Message Sent !", Toast.LENGTH_LONG).show();
                }
            }
        });
        BikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btState == 0) {
//                    biketext.setText("Bike ON");
                    // Writing data to Firebase
                    node.child("BikeOffBtn").setValue("0");
                    BikeBtn.getBackground().setTint(Color.GREEN);
                    btState = 1;
//                    Toast.makeText(RiderDashboard.this, "Bike Turned ON !", Toast.LENGTH_LONG).show();
                } else {
//                    biketext.setText("Bike OFF");
                    // Writing data to Firebase
                    node.child("BikeOffBtn").setValue("1");
                    BikeBtn.getBackground().setTint(Color.WHITE);
                    btState = 0;
//                    Toast.makeText(RiderDashboard.this, "Bike Turned OFF !", Toast.LENGTH_LONG).show();
                }
            }
        });


        AreaRisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RiderDashboard.this, AreaRisk.class);
                startActivity(i);
            }
        });
        LLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latt > 0 && longg > 0) {
                    Intent i = new Intent(RiderDashboard.this, LiveLocation.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(RiderDashboard.this, "Location Not Fetched Yet", Toast.LENGTH_LONG).show();
                }

            }
        });
        KmDriven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RiderDashboard.this, KmDriven.class);
                startActivity(i);
            }
        });
        OilChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RiderDashboard.this, OilChange.class);
                startActivity(i);
            }
        });



    }

    private void addNotificationOil() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(RiderDashboard.this, "Notificcation ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Oil Change Alert")
                .setContentText("Your Oil Change Date has arrived.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(RiderDashboard.this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(6, builder.build());

    }

    private void addNotificationEmergency() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(RiderDashboard.this, "Notificcation ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Emergency Alert")
                .setContentText("Your Rider has activated the SOS Button.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(RiderDashboard.this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());

    }
    private void addNotificationBike() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(RiderDashboard.this, "Notificcation ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Bike Alert")
                .setContentText("Your Rider has activated the Bike On/off Button.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(RiderDashboard.this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(3, builder.build());

    }

    private void addNotificationBattery() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(RiderDashboard.this, "Notificcation ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Battery Alert")
                .setContentText("Your helmet battery health is less than 20%.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(RiderDashboard.this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(4, builder.build());

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("LOG OUT");
        builder.setMessage("Do you want to Log Out ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editor.clear();
                        editor.commit(); // commit changes
                        finish();
                        startActivity(new Intent(RiderDashboard.this, GuardianLogin.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}