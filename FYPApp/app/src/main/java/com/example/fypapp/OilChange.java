package com.example.fypapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class OilChange extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,oilChangenode,kmtodrivenode;
    CalendarView calendar;
    private String Date;
    boolean dateEnabled = false;
    private float SaveKmtodrive=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oil_change);

        calendar = (CalendarView) findViewById(R.id.calendar);
        EditText kmdrive = (EditText) findViewById(R.id.kmdriven);
        TextView oildate = (TextView) findViewById(R.id.select_date);
        Button subBtn = (Button) findViewById(R.id.SubmitBtn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notificcation ID", "Notificcation ID", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        node = db.getReference("OilChange");
        oilChangenode = node.child("KmDrivenOil");
        kmtodrivenode = node.child("KmToDrive");

        kmtodrivenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    SaveKmtodrive = Float.parseFloat(value);
                } catch(NumberFormatException nfe) {
                    Toast.makeText(OilChange.this, "Integer Parse Error", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(OilChange.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        oilChangenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    float num = Float.parseFloat(value);
                    if(num > SaveKmtodrive){
                        addNotificationOil();
                    }
                } catch(NumberFormatException nfe) {
                    Toast.makeText(OilChange.this, "Oil Change Notify Error", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(OilChange.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int monthno = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthno);
        c.set(Calendar.DAY_OF_MONTH, day);
        long milliTime = c.getTimeInMillis();
        calendar.setDate(milliTime, true, true);
        calendar.setMinDate(milliTime);

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String limit = kmdrive.getText().toString();
                if (limit.isEmpty()) {
                    kmdrive.setError("Km to Drive cannot be empty");
                    kmdrive.requestFocus();
                    return;
                }
                if (dateEnabled) {
                    node.child("KmToDrive").setValue(limit);
                    node.child("KmDrivenOil").setValue("0");
                    node.child("date").setValue(Date);
                    Toast.makeText(OilChange.this, "Success !", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OilChange.this, "Select Date !", Toast.LENGTH_LONG).show();
                }
            }
        });


        // Add Listener in calendar
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            // In this Listener have one method and in this method we will get the value of DAYS, MONTH, YEARS
            public void onSelectedDayChange(
                    @NonNull CalendarView view,
                    int year,
                    int month,
                    int dayOfMonth) {

                // Store the value of date with format in String type Variable Add 1 in month because month index is start with 0
                Date = dayOfMonth + "-" + (month + 1) + "-" + year;

                // set this date in EditText for Display
                oildate.setText(Date);
                dateEnabled = true;
            }
        });
    }

    private void addNotificationOil() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(OilChange.this, "Notificcation ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Oil Change Alert")
                .setContentText("Your Oil Change Date has arrived.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(OilChange.this);

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
}