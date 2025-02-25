package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SensorStatus extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,s1node,s2node,s3node,s4node;
    TextView s1view,s2view,s3view,s4view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_status);

        s1view = (TextView) findViewById(R.id.s1);
        s2view = (TextView) findViewById(R.id.s2);
        s3view = (TextView) findViewById(R.id.s3);
        s4view = (TextView) findViewById(R.id.s4);

        node = db.getReference("Sensors");
        s1node = node.child("sensor1");
        s2node = node.child("sensor2");
        s3node = node.child("sensor3");
        s4node = node.child("sensor4");

        s1node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                s1view.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SensorStatus.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        s2node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                s2view.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SensorStatus.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        s3node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                s3view.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SensorStatus.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        s4node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                s4view.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SensorStatus.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }
}