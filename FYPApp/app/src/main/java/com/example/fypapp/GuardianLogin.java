package com.example.fypapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Guard;

import static android.Manifest.permission.READ_CONTACTS;

public class GuardianLogin extends AppCompatActivity {
    private FirebaseAuth auth;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private CheckBox checkBox;
    private TextView signupRedirectText, forgotpass;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_login);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

                if (pref.getBoolean("key_name", false)) {
            Intent myintent = new Intent(GuardianLogin.this, GuardianDashboard.class);
            startActivity(myintent);
            finish();
        }

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.Gpassword);
        loginButton = findViewById(R.id.LoginBtn);
        signupRedirectText = findViewById(R.id.signupText);
        forgotpass = findViewById(R.id.forgotpasswordG);
        checkBox = findViewById(R.id.rememberme);

        node = db.getReference("Credentials");
        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                if (email.isEmpty()) {
                    loginEmail.setError("Email cannot be empty");
                    loginEmail.requestFocus();
                    return;
                } else if (pass.isEmpty()) {
                    loginPassword.setError("Password cannot be empty");
                    loginPassword.requestFocus();
                    return;
                } else {
                    SignIn(email, pass);
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GuardianLogin.this, GuardianSignUp.class));
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                if (email.isEmpty()) {
                    loginEmail.setError("Email cannot be empty");
                    loginEmail.requestFocus();
                    return;
                }
                else if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Intent myintent = new Intent(GuardianLogin.this, ForgotPassword.class);
                    myintent.putExtra("Email", email);
                    startActivity(myintent);
                    finish();
                }
            }
        });
    }

    public void SignIn(String email, String pass) {
        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(GuardianLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent car_intent = new Intent(GuardianLogin.this, GuardianDashboard.class);
                                startActivity(car_intent);
                                if (checkBox.isChecked()) {
                                    editor.putBoolean("key_name", true); // Storing boolean
                                    editor.putString("Email", email); // Storing boolean
                                    editor.commit();
                                }
                                else {
                                        editor.putBoolean("key_name", false); // Storing boolean
                                    editor.commit(); // commit changes
                                    }
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GuardianLogin.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                loginPassword.setError("Empty fields are not allowed");
            }
        } else if (email.isEmpty()) {
            loginEmail.setError("Empty fields are not allowed");
        } else {
            loginEmail.setError("Please enter correct email");
        }
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
                        startActivity(new Intent(GuardianLogin.this, LoginHome.class));
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