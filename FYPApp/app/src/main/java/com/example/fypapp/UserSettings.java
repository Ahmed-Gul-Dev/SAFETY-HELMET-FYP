package com.example.fypapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class UserSettings extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,speednode,emailnode,idnode,speedlimitnode;
    private String email,getName;
    float speedlimit=0;
    private ActivityResultLauncher<String> pushNotificationPermissionLauncher;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notificcation ID", "Notificcation ID", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //Get text from Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("Email");
        Log.e("MainActivity - Email: ", email);


        CardView RiderID=findViewById(R.id.us1);
        TextView rspeed = (TextView) findViewById(R.id.RiderSpeed);
        EditText rspeedlimit = (EditText) findViewById(R.id.RiderSpeedLimit);
        Button setlimit = (Button)findViewById(R.id.setlimit);

        node = db.getReference("Users");
        speednode = node.child("RiderSpeed");
        speedlimitnode = node.child("RiderSpeedLimit");

        speedlimitnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                rspeedlimit.setText(value);
                try {
                    speedlimit = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    // handle the exception
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserSettings.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        speednode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                rspeed.setText(value+" Km/h");
//                try {
//                    int spd = Integer.parseInt(value);
//                    if(spd > speedlimit){
//                        addNotificationSpeed();
//                    }
//                } catch (NumberFormatException e) {
//                    // handle the exception
//                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserSettings.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        setlimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String limit = rspeedlimit.getText().toString();
                if (limit.isEmpty()) {
                    rspeedlimit.setError("Speed Limit cannot be empty");
                    rspeedlimit.requestFocus();
                    return;
                }
                node.child("RiderSpeedLimit").setValue(limit);
                Toast.makeText(UserSettings.this, "Speed Limit Set !", Toast.LENGTH_LONG).show();
            }
        });

        RiderID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String stringSenderEmail = "codemationsolutions@gmail.com";
                    String stringReceiverEmail = email;
                    String stringPasswordSenderEmail = "hkyy bsuz ucqq wksy";

                    String stringHost = "smtp.gmail.com";

                    Properties properties = System.getProperties();

                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.transport.protocol", "smtp");
                    properties.put("mail.smtp.starttls.enable", "true");
                    properties.put("mail.smtp.ssl.enable", "true");
                    properties.put("mail.smtp.host", "smtp.gmail.com");
                    properties.put("mail.smtp.port", "587");


                    javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                        }
                    });

                    MimeMessage mimeMessage = new MimeMessage(session);
                    mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                    mimeMessage.setSubject("Subject: Rider ID");
                    mimeMessage.setText("Rider ID Generated is : 3 \n Password is : 12345678");

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Transport.send(mimeMessage);
//                                Toast.makeText(UserSettings.this, "Success", Toast.LENGTH_LONG).show();
                            }
                            catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    threadAliveChecker(thread);

                } catch (AddressException e) {
                    e.printStackTrace();
                }
                catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    void threadAliveChecker(final Thread thread) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!thread.isAlive()) {
                    // do your work after thread finish


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserSettings.this, "Rider ID Generated !", Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(ForgotPassword.this, GuardianLogin.class);
//                            startActivity(i);
                        }
                    });
                    timer.cancel();
                }else {
                    // do work when thread is running like show progress bar
                }
            }
        }, 500, 500);  // first is delay, second is period
    }
}