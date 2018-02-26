package com.webmarke8.app.gencartdriver.Session;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.webmarke8.app.gencartdriver.Objects.Driver;

import br.vince.easysave.EasySave;

/**
 * Created by u on 20-Dec-17.
 */

public class MyApplication extends Application {


    Context mContext;
    static final String MYPREFERENCES = "MyPrefs";
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    private static final String IS_LOGIN = "IsLoggedIn";




    private static MyApplication mInstance;

    public void onCreate() {
        super.onCreate();
        mInstance = this;
        editor = getSharedPreferences(MYPREFERENCES, MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences(MYPREFERENCES, MODE_PRIVATE);

    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }


    public void clearUserPreference(Context mContext) {

        this.mContext = mContext;
        editor.clear();
        editor.commit();
    }


    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }


    public void createLoginSessionDriver(Driver customer) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString("Type", "Driver");
        new EasySave(getApplicationContext()).saveModel("Driver", customer);
        editor.apply();
    }




    public Driver getLoginSessionDriver() {

        return new EasySave(getApplicationContext()).retrieveModel("Driver", Driver.class);

    }



    public String getType() {
        String value = sharedPreferences.getString("Type", null);
        return value;
    }

    public void setType(String Type) {

        editor.putString("Type", Type);
        editor.apply();

    }



}