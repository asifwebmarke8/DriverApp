package com.webmarke8.app.gencartdriver.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NetworkError;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.ParseError;
import com.android.volley.error.ServerError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.medialablk.easytoast.EasyToast;
import com.webmarke8.app.gencartdriver.Adapter.BottomNavigationViewHelper;
import com.webmarke8.app.gencartdriver.Fragments.Earnings;
import com.webmarke8.app.gencartdriver.Fragments.Favourate;
import com.webmarke8.app.gencartdriver.Fragments.History;
import com.webmarke8.app.gencartdriver.Fragments.Mian;
import com.webmarke8.app.gencartdriver.Fragments.OnRideStatus;
import com.webmarke8.app.gencartdriver.Fragments.Profile;
import com.webmarke8.app.gencartdriver.Objects.RideResponse;
import com.webmarke8.app.gencartdriver.R;
import com.webmarke8.app.gencartdriver.Services.LocationService;
import com.webmarke8.app.gencartdriver.Session.MyApplication;
import com.webmarke8.app.gencartdriver.Utils.AppUtils;
import com.webmarke8.app.gencartdriver.Utils.ServerData;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;

    MyApplication myApplication;
    DatabaseReference databaseReference;
    OnRideStatus onRideStatus;
    Dialog Progress;
    Switch aSwitch;
    String OrderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        myApplication = (MyApplication) getApplicationContext();

        if (myApplication.isRideIn()) {
            Intent intent = new Intent(getApplicationContext(), OnRide.class);
            startActivity(intent);
            finish();
        }
        Progress = AppUtils.LoadingSpinner(MainActivity.this);

        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", true);
        ed.commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new Mian(), "Mian").addToBackStack("").commit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            // extract the extra-data in the Notification
            String msg = extras.getString("OrderID");
            EasyToast.success(getApplicationContext(), msg);
            if (!msg.equals("")) {
                onRideStatus = new OnRideStatus();
                Bundle bundle = new Bundle();
                bundle.putString("OrderID", msg);
                OrderID = msg;
                onRideStatus.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, onRideStatus, "OnRideStatus").addToBackStack("").commit();
                myApplication.setRideStatus("0");

            }
        }


        databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversStatus").child(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail().replaceAll("[^A-Za-z]", ""));
        ;

        aSwitch = (Switch) findViewById(R.id.switchhh);
        if (AppUtils.isMyServiceRunning(LocationService.class, getApplicationContext())) {
            aSwitch.setChecked(true);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                databaseReference.setValue(b);
                ChangeStatus(b);


            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        findViewById(R.id.navigationDrawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenOpenOrCloseDrawer();
            }
        });


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);


        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.Home:
//
                                getSupportFragmentManager().popBackStack();
                                break;
                            case R.id.Profile:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new History(), "History").addToBackStack("").commit();


                                break;
                            case R.id.Favorites:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new Favourate(), "Favorites").addToBackStack("").commit();
                                break;

                        }
                        return true;
                    }
                });


    }

    public void OpenOpenOrCloseDrawer() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.History) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new History(), "History").addToBackStack("").commit();
        }

        if (id == R.id.Earnings) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Earnings(), "History").addToBackStack("").commit();
        }
        if (id == R.id.Profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Profile(), "Profile").addToBackStack("").commit();
        }
        if (id == R.id.Massages) {

            Intent intent = new Intent(getApplicationContext(), Chat_Activity.class);
            startActivity(intent);

        }
        if (id == R.id.Logout) {

            stopService(new Intent(getApplicationContext(), LocationService.class));
            myApplication.logoutUser();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ShowHome() {
        getSupportFragmentManager().popBackStack();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", true);
        ed.commit();
    }

    @Override
    protected void onPause() {

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", false);
        ed.commit();

        super.onPause();
    }

    @Override
    protected void onResume() {

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", true);
        ed.commit();


        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", false);
        ed.commit();

    }

    @Override
    protected void onDestroy() {

        SharedPreferences sp = getSharedPreferences("GenCart", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("ActivityStatus", false);
        ed.commit();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onDestroy();
    }


    private void ChangeStatus(final Boolean Check) {


        Progress.show();
        String ServerUrl = "";
        if (Check) {
            ServerUrl = ServerData.UpdateStatus;
        } else {
            ServerUrl = ServerData.UpdateStatus;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerUrl
                , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Progress.dismiss();
                if (response.contains("1")) {
                    aSwitch.setChecked(true);
                } else {
                    aSwitch.setChecked(false);
                }

                if (Check) {
                    if (!AppUtils.isMyServiceRunning(LocationService.class, MainActivity.this)) {
                        startService(new Intent(getApplicationContext(), LocationService.class));
                        AppUtils.setStatus(getApplicationContext(), Check);
                    }
                } else {
                    stopService(new Intent(getApplicationContext(), LocationService.class));
                    AppUtils.setStatus(getApplicationContext(), Check);
                }
                EasyToast.success(getApplicationContext(), "Status Changed");
                myApplication.setDriverStatus(String.valueOf(Check));


            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Progress.dismiss();
                        aSwitch.setChecked(!Check);
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            EasyToast.error(getApplicationContext(), "Please check your internet Connection");
                        } else if (error instanceof AuthFailureError) {
                            EasyToast.error(getApplicationContext(), "Authentication Error!");
                        } else if (error instanceof ServerError) {
                            EasyToast.error(getApplicationContext(), "Server Side Error!");
                        } else if (error instanceof NetworkError) {
                            EasyToast.error(getApplicationContext(), "Network Error!");
                        } else if (error instanceof ParseError) {
                            EasyToast.error(getApplicationContext(), "Parse Error!");
                        }
                    }
                }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (Check) {
                    params.put("status", "1");
                } else {
                    params.put("status", "0");
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + myApplication.getLoginSessionDriver().getSuccess().getToken());
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    public void ShowStatus() {
        onRideStatus = new OnRideStatus();
        Bundle bundle = new Bundle();
        bundle.putString("OrderID", OrderID);
        onRideStatus.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, onRideStatus, "OnRideStatus").addToBackStack("").commit();
    }
}
