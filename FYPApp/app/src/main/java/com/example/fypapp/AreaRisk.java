package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AreaRisk extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,risknode;
    boolean Enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_risk);

        EditText locSelect = (EditText) findViewById(R.id.login_email);
        EditText timeSelect = (EditText) findViewById(R.id.password);
        TextView riskview = (TextView) findViewById(R.id.risklevel);
        Button subBtn = (Button) findViewById(R.id.SubmitBtn);

        node = db.getReference("AreaRisk");
        risknode = node.child("RiskLevel");

        risknode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                riskview.setText(value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AreaRisk.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String place = locSelect.getText().toString();
                String time = timeSelect.getText().toString();
                if (place.isEmpty()) {
                    locSelect.setError("Location cannot be empty");
                    locSelect.requestFocus();
                    return;
                }
                if (time.isEmpty()) {
                    timeSelect.setError("Time Slot cannot be empty");
                    timeSelect.requestFocus();
                    return;
                }
                node.child("Area").setValue(place);
                node.child("Time").setValue(time);
                node.child("check").setValue("ON");
                Toast.makeText(AreaRisk.this, "Success !", Toast.LENGTH_LONG).show();
            }
        });

    }
}