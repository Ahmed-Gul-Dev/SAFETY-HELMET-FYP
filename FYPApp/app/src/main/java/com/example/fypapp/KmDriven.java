package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class KmDriven extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,totalnode,dailynode,source1node,source2node,desnode1,desnode2,kmnode1,kmnode2;
    TextView address,source2,des1,des2,km1,km2,source1;
    int kmA=0,kmB=0;
    String add1,add2,add3,add4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_km_driven);

        TextView TotalKm = (TextView) findViewById(R.id.kmdrivenT);
        TextView kmDaily = (TextView) findViewById(R.id.kmdrivenDaily);
        source1 = (TextView) findViewById(R.id.parameter13);
        source2 = (TextView) findViewById(R.id.parameter23);
        des1 = (TextView) findViewById(R.id.p12);
        des2 = (TextView) findViewById(R.id.p24);
        address = (TextView) findViewById(R.id.text3);
        km1 = (TextView) findViewById(R.id.p13);
        km2 = (TextView) findViewById(R.id.p25);

        node = db.getReference("KmDriven");
        totalnode = node.child("Total");
        dailynode = node.child("dailyKM");
        source1node = node.child("add1");
        source2node = node.child("add3");
        desnode1 = node.child("add2");
        desnode2 = node.child("add4");
        kmnode1 = node.child("kmA");
        kmnode2 = node.child("kmB");

        source1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address.setText(add1);
            }
        });
        source2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address.setText(add3);
            }
        });
        des1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address.setText(add2);
            }
        });
        des2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address.setText(add4);
            }
        });

        source1node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                add1 = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        source2node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                add3 = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        desnode1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                add2 = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        desnode2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                add4 = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        totalnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                TotalKm.setText(value+" Km");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        dailynode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                kmDaily.setText(value+" Km");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        kmnode1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                km1.setText(value+" Km");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        kmnode2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                km2.setText(value+" Km");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(KmDriven.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

    }
}