package com.example.minimalistserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.minimalistserver.MainActivity;
import com.example.minimalistserver.R;
import com.example.minimalistserver.controller.ControllerHandler;
import com.example.minimalistserver.utils.NotificationHelper;

public class ServerService extends Service {
    private ControllerHandler controllerHandler;

    private MainActivity mainActivity;

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        controllerHandler = new ControllerHandler(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createNotificationChannel(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!controllerHandler.isServerActive()) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                    .setContentTitle("Server Service")
                    .setContentText("Server is running")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(NOTIFICATION_ID, notification);

            SharedPreferences preferences = getSharedPreferences("ServerState", MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putBoolean("isServerRunning", true);
            edit.apply();

            new Thread(() -> {
                try {
                    controllerHandler.startServer();
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }).start();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            if (controllerHandler.isServerActive()) {
                Thread thread = new Thread(() -> {
                    try {
                        controllerHandler.stopServer();
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                });
                thread.start();
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
