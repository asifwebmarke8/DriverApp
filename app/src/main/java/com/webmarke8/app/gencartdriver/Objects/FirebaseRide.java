package com.webmarke8.app.gencartdriver.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GeeksEra on 3/19/2018.
 */

public class FirebaseRide {
    @SerializedName("rideBooking")
   private String RideBooking;
    @SerializedName("rideStatus")
   private String RideStatus;

    public String getRideBooking() {
        return RideBooking;
    }

    public void setRideBooking(String rideBooking) {
        RideBooking = rideBooking;
    }

    public String getRideStatus() {
        return RideStatus;
    }

    public void setRideStatus(String rideStatus) {
        RideStatus = rideStatus;
    }
}
