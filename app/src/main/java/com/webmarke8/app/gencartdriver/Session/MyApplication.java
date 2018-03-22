package com.webmarke8.app.gencartdriver.Session;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.webmarke8.app.gencartdriver.Objects.Driver;
import com.webmarke8.app.gencartdriver.Objects.RideResponse;

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
    private static final String IS_Ride = "IsRideIn";
    private static final String DRIVER_STATUS = "DRIVER_STATUS";
    private static final String RIDE_STATUS = "RIDE_STATUS";


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
        new EasySave(getApplicationContext()).saveModel("Driver", null);
        editor.clear();
        editor.commit();

    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }


    public void createLoginSessionDriver(Driver driver) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString("Type", "Driver");
        new EasySave(getApplicationContext()).saveModel("Driver", driver);
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

    public void setRideStatus(String Status) {

        editor.putString(RIDE_STATUS, Status);
        editor.apply();

    }

    public void setDriverStatus(String Status) {

        editor.putString(DRIVER_STATUS, Status);
        editor.apply();

    }

    public static String getDriverStatus() {
        String value = sharedPreferences.getString(DRIVER_STATUS, null);
        return value;
    }

    public static String getRideStatus() {
        String value = sharedPreferences.getString(RIDE_STATUS, null);
        return value;
    }


    public void logoutRide() {
        editor.putBoolean(IS_Ride, false);
        editor.apply();
    }

    public void setRideTrue() {
        editor.putBoolean(IS_Ride, true);
        editor.apply();
    }

    public boolean isRideIn() {
        return sharedPreferences.getBoolean(IS_Ride, false);
    }


    public void SaveWorkingOrder(RideResponse rideResponse) {
        new EasySave(getApplicationContext()).saveModel("rideResponse", rideResponse);
    }


    public RideResponse GetWorkingOrder() {
        return new EasySave(getApplicationContext()).retrieveModel("rideResponse", RideResponse.class);

    }


    public void setNotificationForArrived(String Status) {
        editor.putString("NotificationForArrived", Status);
        editor.apply();
    }

    public static String getNotificationForArrived() {
        String value = sharedPreferences.getString("NotificationForArrived", "");
        return value;
    }

}