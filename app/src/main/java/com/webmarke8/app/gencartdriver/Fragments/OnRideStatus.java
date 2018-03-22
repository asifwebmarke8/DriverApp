package com.webmarke8.app.gencartdriver.Fragments;


import android.app.Dialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.medialablk.easytoast.EasyToast;
import com.webmarke8.app.gencartdriver.Activities.MainActivity;
import com.webmarke8.app.gencartdriver.Activities.OnRide;
import com.webmarke8.app.gencartdriver.Objects.Driver;
import com.webmarke8.app.gencartdriver.Objects.RideResponse;
import com.webmarke8.app.gencartdriver.R;
import com.webmarke8.app.gencartdriver.Session.MyApplication;
import com.webmarke8.app.gencartdriver.Utils.AppUtils;
import com.webmarke8.app.gencartdriver.Utils.ServerData;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnRideStatus extends Fragment {


    int count = 60;
    TextView Time;
    Timer thread;
    Dialog Progress;
    MyApplication myApplication;
    Bundle bundle;
    ImageView StatusImage, StepImage, StatusProgress;
    TextView StepText;
    FirebaseDatabase database;

    public OnRideStatus() {
        // Required empty public constructor
    }

    TimerTask timer = new TimerTask() {

        @Override
        public void run() {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count--;
                    if (count >= 0) {
                        Time.setText("You Have " + String.valueOf(count) + " seconds to Accept or Decline");
                    }
                }
            });
            if (count <= 0) {
                thread.cancel();
                ((MainActivity) getActivity()).ShowHome();
                AppUtils.ShowNotification(getActivity(), "You Missed One Ride");
            }
        }

    };


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_ride_status, container, false);
        StatusImage = (ImageView) view.findViewById(R.id.StatusImage);
        myApplication = (MyApplication) getActivity().getApplicationContext();
        database = FirebaseDatabase.getInstance();


        StepImage = (ImageView) view.findViewById(R.id.StepImage);
        StepText = (TextView) view.findViewById(R.id.StepText);
        StatusProgress = (ImageView) view.findViewById(R.id.Progress);

        if (myApplication.getRideStatus().equals("0")) {
            bundle = getArguments();
            Progress = AppUtils.LoadingSpinner(getActivity());
            Time = (TextView) view.findViewById(R.id.Timer);
            thread = new Timer();
            thread.scheduleAtFixedRate(timer, 0, 1000);

            view.findViewById(R.id.Accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CancelTimer();
                    ChangeStatus(true);
                    EasyToast.success(getActivity(), "Success");

                }
            });
            view.findViewById(R.id.Reject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CancelTimer();
                    ChangeStatus(false);
                    myApplication.setRideStatus("10");
                    myApplication.logoutRide();
                    myApplication.SaveWorkingOrder(null);
                    database.getReference().child("OrderStatus").child(bundle.getString("OrderID")).setValue("10");
                    EasyToast.success(getActivity(), "Success");

                }
            });

        }
        if (myApplication.getRideStatus().equals("1")) {


            ((OnRide) getActivity()).ChangeToolbarText("Status");
            view.findViewById(R.id.Timer).setVisibility(View.GONE);
            TextView Reject = (TextView) view.findViewById(R.id.Reject);
            Reject.setText("Hide");
            TextView Accept = (TextView) view.findViewById(R.id.Accept);
            Accept.setText("Done");
            StatusImage.setImageDrawable(getResources().getDrawable(R.drawable.steps2active));
            StepImage.setImageDrawable(getResources().getDrawable(R.drawable.step2image));
            StatusProgress.setImageDrawable(getResources().getDrawable(R.drawable.progress1));
            StepText.setText("Collected All items of cart from related stores.");

            view.findViewById(R.id.Accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myApplication.setRideStatus("2");
                    ((OnRide) getActivity()).ShowHome();
                    ((OnRide) getActivity()).ChangeToolbarText("Route");
                    database.getReference().child("OrderStatus").child(bundle.getString("OrderID")).setValue("2");
                    EasyToast.success(getActivity(), "Success");

                }
            });
            view.findViewById(R.id.Reject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OnRide) getActivity()).ShowHome();
                    ((OnRide) getActivity()).ChangeToolbarText("Route");


                }
            });
        }
        if (myApplication.getRideStatus().equals("2")) {

            ((OnRide) getActivity()).ChangeToolbarText("Status");

            TextView Reject = (TextView) view.findViewById(R.id.Reject);
            Reject.setText("Hide");
            view.findViewById(R.id.Timer).setVisibility(View.GONE);

            TextView Accept = (TextView) view.findViewById(R.id.Accept);
            Accept.setText("Finish");
            StatusImage.setImageDrawable(getResources().getDrawable(R.drawable.steps3active));
            StepImage.setImageDrawable(getResources().getDrawable(R.drawable.step3image));
            StatusProgress.setImageDrawable(getResources().getDrawable(R.drawable.progress2));
            StepText.setText("Deliver all cart items of that Customer.");
            view.findViewById(R.id.Accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myApplication.setRideStatus("0");
                    database.getReference().child("OrderStatus").child(bundle.getString("OrderID")).setValue("3");
                    myApplication.logoutRide();
                    myApplication.SaveWorkingOrder(null);
                    ((OnRide) getActivity()).ShowHome();
                    ((OnRide) getActivity()).ChangeToolbarText("Route");
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                    EasyToast.success(getActivity(), "Success");


                }
            });

            view.findViewById(R.id.Reject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OnRide) getActivity()).ShowHome();
                    ((OnRide) getActivity()).ChangeToolbarText("Route");

                }
            });
        }


        return view;


    }


    private void ChangeStatus(final Boolean Check) {


        Progress.show();
        String ServerUrl = "";
        if (Check) {
            ServerUrl = ServerData.AcceptOrder;
        } else {
            ServerUrl = ServerData.AcceptOrder;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerUrl
                , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Progress.dismiss();

                CancelTimer();
                if (Check) {
                    myApplication.setRideTrue();
                    GetDetails(bundle.getString("OrderID"));

                } else {
                    CancelTimer();
                    ((MainActivity) getActivity()).ShowHome();
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CancelTimer();
                        Progress.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            EasyToast.error(getActivity(), "Please check your internet Connection");
                        } else if (error instanceof AuthFailureError) {
                            EasyToast.error(getActivity(), "Authentication Error!");
                        } else if (error instanceof ServerError) {
                            EasyToast.error(getActivity(), "Server Side Error!");
                        } else if (error instanceof NetworkError) {
                            EasyToast.error(getActivity(), "Network Error!");
                        } else if (error instanceof ParseError) {
                            EasyToast.error(getActivity(), "Parse Error!");
                        }
                    }
                }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("order_id", bundle.getString("OrderID"));
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + myApplication.getLoginSessionDriver().getSuccess().getToken());
                headers.put("Accept", "application/json");
                return headers;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    public void CancelTimer() {
        if (thread != null)
            thread.cancel();
    }


    private void GetDetails(String OrderID) {


        Progress.show();
        String ServerUrl = ServerData.GetOrderDetails + OrderID;


        StringRequest stringRequest = new StringRequest(Request.Method.GET, ServerUrl
                , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Progress.dismiss();

                try {
                    Gson gson = new Gson();
                    RideResponse rideResponse = null;
                    JSONArray jsonArray = new JSONArray(response);
                    response = jsonArray.getJSONObject(0).toString();
                    rideResponse = gson.fromJson(response, RideResponse.class);
                    if (rideResponse != null) {
                        myApplication.setRideStatus("1");
                        Intent intent = new Intent(getActivity(), OnRide.class);
                        rideResponse.setOrderID(bundle.getString("OrderID"));
                        myApplication.SaveWorkingOrder(rideResponse);
                        getActivity().startActivity(intent);
                    }

                } catch (Exception Ex) {
                    EasyToast.error(getActivity(), "Server Side Error! Parsing");

                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Progress.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            EasyToast.error(getActivity(), "Please check your internet Connection");
                        } else if (error instanceof AuthFailureError) {
                            EasyToast.error(getActivity(), "Authentication Error!");
                        } else if (error instanceof ServerError) {
                            EasyToast.error(getActivity(), "Server Side Error!");
                        } else if (error instanceof NetworkError) {
                            EasyToast.error(getActivity(), "Network Error!");
                        } else if (error instanceof ParseError) {
                            EasyToast.error(getActivity(), "Parse Error!");
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + myApplication.getLoginSessionDriver().getSuccess().getToken());
                headers.put("Accept", "application/json");
                return headers;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }
}
