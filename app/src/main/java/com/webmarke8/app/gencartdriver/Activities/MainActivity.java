package com.webmarke8.app.gencartdriver.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.webmarke8.app.gencartdriver.Adapter.BottomNavigationViewHelper;
import com.webmarke8.app.gencartdriver.Fragments.Earnings;
import com.webmarke8.app.gencartdriver.Fragments.Favourate;
import com.webmarke8.app.gencartdriver.Fragments.History;
import com.webmarke8.app.gencartdriver.Fragments.Profile;
import com.webmarke8.app.gencartdriver.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


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
//                                getSupportFragmentManager().beginTransaction()
//                                        .replace(R.id.container, new StoreFragment(),"Home").commit();
                                break;
                            case R.id.Profile:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new History(), "History").commit();


                                break;
                            case R.id.Favorites:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new Favourate(), "Favorites").commit();
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
                    .replace(R.id.container, new History(), "History").commit();
        }

        if (id == R.id.Earnings) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Earnings(), "History").commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
