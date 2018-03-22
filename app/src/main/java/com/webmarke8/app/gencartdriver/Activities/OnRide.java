package com.webmarke8.app.gencartdriver.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.webmarke8.app.gencartdriver.Fragments.OnRideStatus;
import com.webmarke8.app.gencartdriver.Manifest;
import com.webmarke8.app.gencartdriver.Objects.Driver;
import com.webmarke8.app.gencartdriver.Objects.RideResponse;
import com.webmarke8.app.gencartdriver.R;
import com.webmarke8.app.gencartdriver.Session.MyApplication;
import com.webmarke8.app.gencartdriver.Utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class OnRide extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {

    GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    RideResponse rideResponse;
    private Location mLastKnownLocation;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.primary_dark_material_light};
    FirebaseDatabase database;
    TextView CustomerName, CustomerAddress;
    TextView Route;
    MyApplication myApplication;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_ride);

        dialog = AppUtils.LoadingSpinner(OnRide.this);
        dialog.show();
        polylines = new ArrayList<>();

        myApplication = (MyApplication) getApplicationContext();

        if (myApplication.isRideIn()) {
            rideResponse = myApplication.GetWorkingOrder();
            dialog.dismiss();

        } else {
            dialog.dismiss();
            finish();
        }

        Route = (TextView) findViewById(R.id.Route);

        database = FirebaseDatabase.getInstance();
        database.getReference().child("OrderStatus").child(rideResponse.getOrderID()).setValue("1");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(OnRide.this);
        getLocationPermission();
        if (getServicesAvailable()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.StartChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Chat_Activity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Rideresponse", rideResponse);
                intent.putExtra("response", bundle);
                startActivity(intent);
            }
        });
        findViewById(R.id.ChangeStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OnRideStatus onRideStatus = new OnRideStatus();
                Bundle bundle = new Bundle();
                bundle.putString("OrderID", rideResponse.getOrderID());
                onRideStatus.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, onRideStatus, "OnRideStatus").addToBackStack("").commit();


            }
        });
        CustomerName = (TextView) findViewById(R.id.CustomerName);
        CustomerAddress = (TextView) findViewById(R.id.CustomerAddress);
        CustomerName.setText(rideResponse.getCustomer().getName());
        CustomerAddress.setText(rideResponse.getCustomer().getAddress());


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        updateLocationUI();
        buildGoogleApiClient();

        if (rideResponse != null) {

            String[] UserSplit = rideResponse.getCustomer().getLat_long().split(",");
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(UserSplit[0]), Double.parseDouble(UserSplit[1]))).
                    flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Double.parseDouble(UserSplit[0]), Double.parseDouble(UserSplit[1])), DEFAULT_ZOOM));
            for (RideResponse.StoresObject storesObject : rideResponse.getStores()) {


                String[] Split = storesObject.getLat_long().split(",");
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(Split[0]), Double.parseDouble(Split[1]))).
                        flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.store_marker)));
            }

        }


    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(OnRide.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    // Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Creating location request object
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public static int UPDATE_INTERVAL = 100; // 3 sec
    public static int FATEST_INTERVAL = 100; // 5 sec
    public static int DISPLACEMENT = 0; // 10 meters

    //Starting the location updates
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    //Stopping location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) OnRide.this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    public void onLocationChanged(Location location) {
        // Assign the new location

        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            } else if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }

        mLastKnownLocation = location;
        displayLocation();
        String[] UserSplit = rideResponse.getCustomer().getLat_long().split(",");
        Location User = new Location("");
        User.setLatitude(Double.parseDouble(UserSplit[0]));
        User.setLongitude(Double.parseDouble(UserSplit[1]));
        if (GetDistance(User) < 1.0) {

            if (myApplication.getRideStatus().equals("2") && myApplication.getNotificationForArrived().equals("")) {
                myApplication.setNotificationForArrived("1");
                AppUtils.ShowNotification(getApplicationContext(), "are you arrived at customer side");
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Method to display the location on UI
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {


            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastKnownLocation != null) {
                double latitude = mLastKnownLocation.getLatitude();
                double longitude = mLastKnownLocation.getLongitude();
                eraseLines();
                String[] UserSplit = rideResponse.getCustomer().getLat_long().split(",");
                makeDirection(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new LatLng(Double.parseDouble(UserSplit[0]), Double.parseDouble(UserSplit[1])));
            }
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
//        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getServicesAvailable();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public boolean getServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onRoutingFailure(RouteException e) {

        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int ii) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mMap.moveCamera(center);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    public void makeDirection(LatLng start, LatLng end) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();
    }

    public void eraseLines() {
        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines.clear();
    }


    public void ChangeToolbarText(String Text) {
        Route.setText(Text);
    }

    public void ShowHome() {
        getSupportFragmentManager().popBackStack();
    }

    public Double GetDistance(Location location) {
        return getMiles(Double.parseDouble(String.valueOf(mLastKnownLocation.distanceTo(location))));
    }

    public Double getMiles(Double i) {
        return i * 0.000621371192;
    }

}
