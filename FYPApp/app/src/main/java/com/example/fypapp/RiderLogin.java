package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderLogin extends AppCompatActivity {
    private FirebaseAuth auth;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,idnode,passnode;
    private EditText riderID, riderPassword;
    private Button loginButton;
    private CheckBox checkBox;
    private TextView signupRedirectText;
    public SharedPreferences.Editor editor;
    String str1,str2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_login);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("RiderPref", 0); // 0 - for private mode
        editor = pref.edit();

        if (pref.getBoolean("key_name", false)) {
            Intent myintent = new Intent(RiderLogin.this, RiderDashboard.class);
            startActivity(myintent);
            finish();
        }


        riderID = findViewById(R.id.rider_id);
        riderPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.LoginBtn);
        checkBox = findViewById(R.id.rememberme);

        node = db.getReference("Users");

        idnode = node.child("ID");
        passnode = node.child("RiderPass");

        idnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                str1 = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderLogin.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        passnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                str2 = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RiderLogin.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = riderID.getText().toString().trim();
                String pass = riderPassword.getText().toString().trim();

                if (id.isEmpty()) {
                    riderID.setError("ID cannot be empty");
                    riderID.requestFocus();
                    return;
                } else if (pass.isEmpty()) {
                    riderPassword.setError("Password cannot be empty");
                    riderPassword.requestFocus();
                    return;
                } else {
                    if (id.equals(str1) && pass.equals(str2)) {
                        Toast.makeText(RiderLogin.this, "Success", Toast.LENGTH_LONG).show();
                        Intent car_intent = new Intent(RiderLogin.this, RiderDashboard.class);
                        startActivity(car_intent);
                        if (checkBox.isChecked()) {
                            editor.putBoolean("key_name", true); // Storing boolean
                            editor.commit();
                        }
                        else {
                            editor.putBoolean("key_name", false); // Storing boolean
                            editor.commit(); // commit changes
                        }
                        finish();
                    }else{
                        Toast.makeText(RiderLogin.this, "Invalid ID or Password", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EXIT");
        builder.setMessage("Are you sure you want to Exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(new Intent(RiderLogin.this, LoginHome.class));
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