package com.example.fypapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

public class ForgotPassword extends AppCompatActivity {
    private ActivityResultLauncher<String> pushNotificationPermissionLauncher;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    Button sendBtn;
    String EmailSendTo;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node, email1,email2,email3,passnode;
    String emailsubject;
    long nodecount = 0;
    boolean minUser = false;
    String[] list;
    int count=0;
    boolean enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Get text from Intent
        Intent intent = getIntent();
        EmailSendTo = intent.getStringExtra("Email");
        Log.e("ForgotPassword - Email: ", EmailSendTo);
//        EmailSendTo = "muhammadahmedgul@gmail.com";

//        email = findViewById(R.id.login_emailForgot);
        sendBtn = findViewById(R.id.forgetpassword);
        node = db.getReference("Credentials");
        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    nodecount = (dataSnapshot.getChildrenCount());
                    Log.e("User IDs: ", String.valueOf(nodecount));
                    if(nodecount > 0){
                        minUser = true;
                        EmailCheck(EmailSendTo);
                    }else{
                        minUser = false;
                        Toast.makeText(ForgotPassword.this, "No User exists", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(enabled){
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
                    if (connected) {
                        Toast.makeText(ForgotPassword.this, "Password Sent Success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Password Sent Failed", Toast.LENGTH_SHORT).show();
                    }

                    try {
                        String stringSenderEmail = "codemationsolutions@gmail.com";
//                    String stringReceiverEmail = "muhammadahmedgul@gmail.com";
                        String stringReceiverEmail =EmailSendTo;
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

                        mimeMessage.setSubject("Subject: Guardian Login Password");
                        mimeMessage.setText("Your Saved password is : "+emailsubject);



                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Transport.send(mimeMessage);
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
                }else{
                    Toast.makeText(ForgotPassword.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void EmailCheck(String data) {


//        Log.e("User IDs: ", String.valueOf(nodecount));
        list = new String[(int) nodecount];
        if(minUser) {
            if (nodecount == 1) {
                email1 = node.child("1").child("Email");
                email1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[0] = String.valueOf(value);
                        Log.e("Email # 1: ", list[0]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            } else if (nodecount == 2) {
                email1 = node.child("1").child("Email");
                email1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[0] = String.valueOf(value);
                        Log.e("Email # 2: ", list[0]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
                email2 = node.child("2").child("Email");
                email2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[1] = String.valueOf(value);
                        Log.e("Email # 2: ", list[1]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            } else if (nodecount == 3) {
                email1 = node.child("1").child("Email");
                email1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[0] = String.valueOf(value);
                        Log.e("Email # 3: ", list[0]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
                email2 = node.child("2").child("Email");
                email2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[1] = String.valueOf(value);
                        Log.e("Email # 3: ", list[1]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
                email3 = node.child("3").child("Email");
                email3.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class); // Patient Name
                        list[2] = String.valueOf(value);
                        Log.e("Email # 3: ", list[2]);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = 0;
                    for (int i = 1; i <= nodecount; i++) {
                        if (data.equals(list[count])) {
                            Log.e("Matched User: ", data);
                            Log.e("Matched Node : ", String.valueOf(i));
                            enabled = true;
                            passnode = node.child(String.valueOf(i)).child("pass");
                        }
                        count = count + 1;
                    }
                    if(enabled){
                        passnode.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                emailsubject  = dataSnapshot.getValue(String.class);
                                Log.e("Password : ", emailsubject);
                            }
                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }, 500);
        }
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
//                            Toast.makeText(ForgotPassword.this, "", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(ForgotPassword.this, GuardianLogin.class);
                            startActivity(i);
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