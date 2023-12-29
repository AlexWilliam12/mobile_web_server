package com.example.minimalistserver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.minimalistserver.services.ServerService;

public class MainActivity extends AppCompatActivity {

    private Button serverControlButton;
    private boolean isServerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverControlButton = findViewById(R.id.buttonServerControl);
        updateButtonState();
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
//        outState.putBoolean("isServerRunning", isServerRunning);
//        super.onSaveInstanceState(outState, outPersistentState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        isServerRunning = savedInstanceState.getBoolean("isServerRunning");
//        System.out.println(isServerRunning);
//        updateButtonState();
//    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("ServerState", MODE_PRIVATE);
        isServerRunning = preferences.getBoolean("isServerRunning", false);
        updateButtonState();
    }

    public void onServerControlButtonClick(View view) {
        if (isServerRunning) stopServer();
        else startServer();
    }

    private void startServer() {
        startService(new Intent(this, ServerService.class));
        this.isServerRunning = true;
        updateButtonState();
    }

    private void stopServer() {
        stopService(new Intent(this, ServerService.class));
        this.isServerRunning = false;
        updateButtonState();
    }

    public void updateButtonState() {
        if (isServerRunning) serverControlButton.setText("Stop Server");
        else serverControlButton.setText("Start Server");
    }


}