package com.example.notificationtext;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 =new Intent(MainActivity.this,NotificationActivity.class);
                PendingIntent pendingIntent=PendingIntent.getActivity(MainActivity.this,0, intent1,0);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(MainActivity.this).setContentTitle("this is my title")
                        .setContentText("notificition1  ")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/")))
                       // .setVibrate(new long[]{0,1000,1000,1000})
                        .setLights(Color.GREEN,1000,1000)
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText("fsfsdfsdfsdfhrthyj   sdv  sds tkryweewrgwtheryilhjdsba"))
                        //.setStyle(new NotificationCompat.BigPictureStyle().bigPicture((BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();
                manager.notify(1, notification);

            }
        });
    }}


