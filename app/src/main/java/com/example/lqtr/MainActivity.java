package com.example.lqtr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String userId;
    private boolean trackingEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = retrieveUserId();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initTrackingSwitch();
    }

    public String retrieveUserId() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String userId = preferences.getString("user_id", null);

        if (userId == null) {
            userId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_id", userId);
            editor.apply();
        }

        return userId;
    }

    public void initTrackingSwitch() {
        Switch trackingSwitch = findViewById(R.id.tracking_enabled);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        trackingEnabled = preferences.getBoolean("tracking_enabled", false);
        trackingSwitch.setChecked(LocationPermission.granted(this) && trackingEnabled);

        if (trackingEnabled) initLocationService();

        trackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTrackingEnabled(isChecked);

                if (isChecked) {
                    MainActivity.this.initLocationService();
                }
            }

            public void setTrackingEnabled(boolean trackingEnabled) {
                MainActivity.this.trackingEnabled = trackingEnabled;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("tracking_enabled", trackingEnabled);
                editor.apply();
            }
        });
    }

    public void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                1
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initLocationService();
                } else {
                    Switch trackingSwitch = findViewById(R.id.tracking_enabled);
                    trackingSwitch.setChecked(false);
                }
            }
        }
    }

    public void initLocationService() {
        if (!LocationPermission.granted(this)) {
            requestLocationPermissions();
        } else {
            WorkManager.getInstance(this).cancelAllWorkByTag("uploadLocationWorker");

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            WorkRequest uploadLocationWorkRequest = new PeriodicWorkRequest.Builder(
                    UploadLocationWorker.class, 15, TimeUnit.MINUTES
            )
                    .setConstraints(constraints)
                    .addTag("uploadLocationWorker")
                    .build();

            WorkManager.getInstance(this).enqueue(uploadLocationWorkRequest);
        }
    }
}