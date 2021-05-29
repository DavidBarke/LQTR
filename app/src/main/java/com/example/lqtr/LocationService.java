package com.example.lqtr;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class LocationService implements LocationListener {

    private static final long MIN_DISTANCE = 50;
    private static final long MIN_TIME = 10000;

    private static LocationService instance = null;

    private LocationManager locationManager;
    public Location location;

    // Singleton
    public static LocationService getLocationManager(Context context)     {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    // Local constructor
    private LocationService( Context context )     {
        initLocationService(context);
    }

    private void initLocationService(Context context) {
        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try   {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) return;

            // Get GPS and network status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE,
                        this
                );
            }

            if (isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE,
                        this
                );
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        System.out.println("onLocationChanged");
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("onStatusChanged");
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        System.out.println("onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        System.out.println("onProviderDisabled");
    }
}
