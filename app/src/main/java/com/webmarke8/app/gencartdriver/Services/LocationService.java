package com.webmarke8.app.gencartdriver.Services;

/**
 * Created by GeeksEra on 3/3/2018.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.medialablk.easytoast.EasyToast;
import com.webmarke8.app.gencartdriver.Activities.MainActivity;
import com.webmarke8.app.gencartdriver.Activities.OnRide;
import com.webmarke8.app.gencartdriver.Objects.FirebaseRide;
import com.webmarke8.app.gencartdriver.R;
import com.webmarke8.app.gencartdriver.Session.MyApplication;

public class LocationService extends Service {
    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1;
    FirebaseDatabase database;
    MyApplication myApplication;
    DatabaseReference databaseReference;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            databaseReference.setValue(new LatLng(location.getLatitude(), location.getLongitude()));
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    /*
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    */

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        initializeLocationManager();
        myApplication = (MyApplication) getApplicationContext();
        FirebaseApp.initializeApp(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail().replaceAll("[^A-Za-z]", ""));


        GetRide();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }


    }

    public void GetRide() {
        FirebaseRide checking = new FirebaseRide();
        checking.setRideBooking("");
        checking.setRideStatus("0");
        FirebaseDatabase.getInstance().getReference().child("DriverBookings").child(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail().replaceAll("[^A-Za-z]", "")).setValue(checking);
        Query query = database.getReference().child("DriverBookings").child(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail().replaceAll("[^A-Za-z]", ""));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                FirebaseRide firebaseRide = dataSnapshot.getValue(FirebaseRide.class);
                Log.w("Hello", "Failed to read value." + dataSnapshot.toString());

                if (firebaseRide.getRideStatus().equals("1")) {
                    SharedPreferences shared = getSharedPreferences("GenCart", MODE_PRIVATE);
                    if (!shared.getBoolean("ActivityStatus", false)) {
                        ShowNotification(firebaseRide.getRideBooking());
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("OrderID", firebaseRide.getRideBooking());
                        ComponentName cn = intent.getComponent();
                        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                        mainIntent.putExtra("OrderID", firebaseRide.getRideBooking());
                        startActivity(mainIntent);
                        sendMessage(1);
                    }

                } else {

//                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                    ComponentName cn = intent.getComponent();
//                    Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
//                    startActivity(mainIntent);
//                    sendMessage(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException());
            }
        });
    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage(int message) {
        Intent intent = new Intent("GenCart");
        intent.putExtra("type", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: " + LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void ShowNotification(String OrderID) {

        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra("OrderID", OrderID);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //set

        builder.setContentIntent(pendIntent);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentText("You Have new Booking");
        builder.setContentTitle("GenCart");
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();
        nm.notify((int) System.currentTimeMillis(), notification);

    }

}
