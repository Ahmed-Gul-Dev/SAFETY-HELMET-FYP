package com.example.fypapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.ybs.passwordstrengthmeter.PasswordStrength;

import java.util.regex.Pattern;

public class GuardianSignUp extends AppCompatActivity implements TextWatcher {
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, signupUsername;
    private Button signupButton;
    private TextView loginRedirectText;
    long node_count = 0;
    boolean enabled = false;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    boolean maxuser=false;
    long nodecount=0;
    private RadioGroup radioGroup;
    RadioButton radioButtonM,radioButtonF;
    String gender = " ";

    // defining our own password pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_sign_up);

        auth = FirebaseAuth.getInstance();
        signupUsername = findViewById(R.id.Susername);
        signupEmail = findViewById(R.id.Email);
        signupPassword = findViewById(R.id.Signuppassword);
        signupButton = findViewById(R.id.SignUpBtn);
        loginRedirectText = findViewById(R.id.LoginText);
        // on below line we are initializing our variables.
        radioGroup = findViewById(R.id.radio_group);
        radioButtonM = findViewById(R.id.radio1);
        radioButtonF = findViewById(R.id.radio2);

        signupPassword.addTextChangedListener(this);

        node = db.getReference("Credentials");
        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    nodecount = (dataSnapshot.getChildrenCount());
                    Log.e("User IDs: ", String.valueOf(nodecount));
                    if (nodecount >= 3) {
                        maxuser=true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GuardianSignUp.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = signupUsername.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                if(radioButtonM.isChecked()){
                    gender = radioButtonM.getText().toString();
                }else if(radioButtonF.isChecked()){
                    gender = radioButtonF.getText().toString();
                }
                if (user.isEmpty()) {
                    signupUsername.setError("Username cannot be empty");
                    Toast.makeText(GuardianSignUp.this, "Enter Username", Toast.LENGTH_SHORT).show();
                    signupUsername.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                    Toast.makeText(GuardianSignUp.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    signupEmail.requestFocus();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signupEmail.setError("Email format is invalid");
                    Toast.makeText(GuardianSignUp.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                    signupEmail.requestFocus();
                    return;
                }
                else {
                    if (maxuser == false) {
                        if (enabled) {
                            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        node.child(String.valueOf(nodecount + 1)).child("Email").setValue(email);
                                        node.child(String.valueOf(nodecount + 1)).child("pass").setValue(pass);
                                        node.child(String.valueOf(nodecount + 1)).child("user").setValue(user);
                                        node.child(String.valueOf(nodecount + 1)).child("gender").setValue(gender);
                                        Toast.makeText(GuardianSignUp.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GuardianSignUp.this, GuardianLogin.class));
                                        finish();
                                    } else {
                                        Toast.makeText(GuardianSignUp.this, "SignUp Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(GuardianSignUp.this, "Weak Password !", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(GuardianSignUp.this, "Users Limit Reached", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GuardianSignUp.this, GuardianLogin.class));
            }
        });

    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(
            CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updatePasswordStrengthView(s.toString());
    }

    private void updatePasswordStrengthView(String password) {
        TextView strengthView = (TextView) findViewById(R.id.text3);
        if (TextView.VISIBLE != strengthView.getVisibility())
            return;

        if (password.isEmpty()) {
            strengthView.setText("");
            return;
        }
        PasswordStrength str = PasswordStrength.calculateStrength(password);
        strengthView.setText(str.getText(this));
        strengthView.setTextColor(str.getColor());
        if (str.getText(this).equals("Weak")) {
            enabled = false;
        }
        else if (str.getText(this).equals("Strong")) {
            enabled = true;
        }
    }

}