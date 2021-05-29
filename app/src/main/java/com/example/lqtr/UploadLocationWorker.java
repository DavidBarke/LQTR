package com.example.lqtr;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadLocationWorker extends ListenableWorker {
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Context context;
    private final UploadLocationService uploadLocationService;

    public UploadLocationWorker(Context context, WorkerParameters parameters) {
        super(context, parameters);

        this.context = context;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        uploadLocationService = WebAPI.getInstance().getService(UploadLocationService.class);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    Location location = locationResult.getLastLocation();

                    uploadLocation(location);

                    fusedLocationProviderClient.removeLocationUpdates(this);

                    completer.set(Result.success());
                }
            };

            requestCurrentLocation(locationCallback);

            return locationCallback;
        });
    }

    private void requestCurrentLocation(LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void uploadLocation(Location location) {
        uploadLocationService.addLocationTs(
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MjE1MjMzNTEsInVzZXJfaWQiOjEsInVzZXIiOiJEYXZpZCIsInN0YXR1cyI6ImFkbWluIn0.gEZ41ELrJN4Z5ANhGSv-4wiTdWESODjpTPLxIojcns0",
                location.getLatitude(),
                location.getLongitude(),
                location.getTime()
        ).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body());
                } else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
