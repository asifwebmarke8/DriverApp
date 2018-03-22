package com.webmarke8.app.gencartdriver.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.webmarke8.app.gencartdriver.Adapter.ChatAdapter;
import com.webmarke8.app.gencartdriver.Objects.Chat_Object;
import com.webmarke8.app.gencartdriver.Objects.Customer;
import com.webmarke8.app.gencartdriver.Objects.RideResponse;
import com.webmarke8.app.gencartdriver.R;
import com.webmarke8.app.gencartdriver.Session.MyApplication;
import com.webmarke8.app.gencartdriver.Utils.AppUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Chat_Activity extends AppCompatActivity {


    EditText edtMessage;
    Button btnSendMessage;
    Dialog progressDialog;
    DatabaseReference databaseReference;
    String receiverEmail;
    FirebaseDatabase database;
    List<Chat_Object> AllMessagesList;
    RecyclerView recycle;
    MyApplication myApplication;
    Bundle bundle;
    RideResponse rideResponse;
    Customer customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_);

        bundle = getIntent().getExtras().getBundle("response");
        rideResponse = (RideResponse) bundle.getSerializable("Rideresponse");

        recycle = (RecyclerView) findViewById(R.id.recycle);
        myApplication = (MyApplication) getApplicationContext();

        customer = new Customer();
        customer.setEmail(rideResponse.getCustomer().getEmail());
        customer.setUsername(rideResponse.getCustomer().getName());
        String[] Split = rideResponse.getCustomer().getLat_long().split(",");
        customer.setLat(Split[0]);
        customer.setLat(Split[1]);
        receiverEmail = customer.getEmail();

        TextView OrderNO = (TextView) findViewById(R.id.OrderNO);
        OrderNO.setText("Order no. " + rideResponse.getOrderID());
        TextView Name = (TextView) findViewById(R.id.Name);
        Name.setText(rideResponse.getCustomer().getName());


//        findViewById(R.id.navigation).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                ((MainActivity) getActivity()).OpenOpenOrCloseDrawer();
//            }
//        });

        findViewById(R.id.Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Chat_Activity.super.onBackPressed();
            }
        });


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        edtMessage = (EditText) findViewById(R.id.edtMessage);
        btnSendMessage = (Button) findViewById(R.id.btnSendMessage);
        progressDialog = AppUtils.LoadingSpinner(Chat_Activity.this);


        databaseReference = FirebaseDatabase.getInstance().getReference();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Upload(view);
            }
        });


        LoadMessages();
    }


    public void Upload(View view) {

        if (edtMessage.equals("")) {
            edtMessage.setError("Cant Empty");
        } else {


            progressDialog.show();

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(rideResponse.getOrderID());
            String Message = edtMessage.getText().toString();

            String receiverName = customer.getUsername();
            receiverEmail = customer.getEmail();
            Chat_Object chat_object = new Chat_Object();
            chat_object.setMessage(Message);
            chat_object.setReciverEmail(receiverEmail);
            chat_object.setReciverName(receiverName);
            chat_object.setSenderEmail(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail());
            chat_object.setSenderName(myApplication.getLoginSessionDriver().getSuccess().getUser().getName());
            DateFormat df = new SimpleDateFormat("HH:mm");
            String Time = df.format(Calendar.getInstance().getTime());
            chat_object.setSendTime(Time);

            String id = databaseReference.push().getKey();
            databaseReference.child(id).setValue(chat_object);
            LoadMessages();

        }
        edtMessage.setText("");
    }

    public void LoadMessages() {


        database = FirebaseDatabase.getInstance();

        Query query = database.getReference().child("Chat").child(rideResponse.getOrderID());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                AllMessagesList = new ArrayList<Chat_Object>();
                AllMessagesList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {


                    Chat_Object chat_object = dataSnapshot1.getValue(Chat_Object.class);


                    if (chat_object.getSenderEmail().contains(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail()) && (chat_object.getReciverEmail().contains(String.valueOf(receiverEmail))) || chat_object.getSenderEmail().contains(String.valueOf(receiverEmail)) && (chat_object.getReciverEmail().contains(String.valueOf(myApplication.getLoginSessionDriver().getSuccess().getUser().getEmail())))) {
                    }
                    AllMessagesList.add(chat_object);

                }
                LoadChattingHistory();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException());
            }
        });


    }


    public void LoadChattingHistory() {
        progressDialog.dismiss();
        LinearLayoutManager verticalLayoutmanager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        ChatAdapter chatAdapter = new ChatAdapter(AllMessagesList, Chat_Activity.this);
        recycle.setLayoutManager(verticalLayoutmanager);
        verticalLayoutmanager.setStackFromEnd(true);
        recycle.setItemAnimator(new DefaultItemAnimator());
        recycle.setAdapter(chatAdapter);

    }

}
