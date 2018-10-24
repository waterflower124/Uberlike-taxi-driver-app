package pe.com.asur.asurpasajero;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.Model.DataMessage;
import pe.com.asur.asurpasajero.Model.FCMResponse;
import pe.com.asur.asurpasajero.Model.Token;
import pe.com.asur.asurpasajero.Remote.IFCMService;
import pe.com.asur.asurpasajero.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance;
    Button btnCancel,btnAccept;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String customerId;
    String orderID;

    String lat,lng;

    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);
//Toast.makeText(getApplicationContext(), "orderererer", Toast.LENGTH_LONG).show();

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        //initView
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtTime = (TextView)findViewById(R.id.txtTime);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait for accept");
        progressDialog.setCancelable(false);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                progressDialog.show();

//                Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
//
//                DatabaseReference driver = FirebaseDatabase.getInstance().getReference("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                driver.removeValue();
//                Common.acceptance = true;


                FirebaseDatabase db = FirebaseDatabase.getInstance();
                final DatabaseReference pick_request = db.getReference(Common.order_table);


                pick_request.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!Common.acceptance) {
                            if(dataSnapshot.hasChild(Common.orderID)) {

                                Common.acceptance = true;
                                pick_request.child(Common.orderID).removeValue();

                                Token token = new Token(customerId);

//                                Toast.makeText(getApplicationContext(), "cancelID::" + customerId, Toast.LENGTH_LONG).show();

                                // Notification notification = new Notification("Cancelado!!","Chofer Cancelo");
                                // Sender sender = new Sender(token.getToken(),notification);

                                Map<String,String> content=new HashMap<>();
                                content.put("title","Accept");
                                content.put("message","Accept your Request");
                                content.put("driverID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                content.put("orderID", orderID);
//                                DataMessage dataMessage=new DataMessage(token.getToken(),content);
                                DataMessage dataMessage=new DataMessage(Common.passengerID, content);
                                mFCMService.sendMessage(dataMessage)
                                        .enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                if (response.body().success == 1)
                                                {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CustommerCall.this, "Success send your Acceptance", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<FCMResponse> call, Throwable t) {

                                            }
                                        });

//                                DatabaseReference accept_ref = FirebaseDatabase.getInstance().getReference(Common.token_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("accept");
//                                accept_ref.setValue("true");

                                Common.current_orerder_state = "w";

                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                final DatabaseReference order_table = db.getReference(Common.driver_tbl);
                                order_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("w");


                                Intent intent = new Intent(CustommerCall.this, DriverTracking.class);
                                //send customer location to new activity
                                intent.putExtra("lat", lat);
                                intent.putExtra("lng", lng);
                                intent.putExtra("customerId", customerId);

                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "This order has already been reserved. Your acceptance is cancelled", Toast.LENGTH_LONG).show();
                                Common.ordering = false;
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference pick_request = db.getReference(Common.order_table);

        pick_request.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

//                Toast.makeText(getApplicationContext(), "delete::" + dataSnapshot.getKey(), Toast.LENGTH_LONG).show();

                if(!Common.acceptance) {
                    if (Common.orderID.equals(dataSnapshot.getKey())) {
                        Toast.makeText(getApplicationContext(), "This order has already been reserved.", Toast.LENGTH_LONG).show();
                        Common.ordering = false;
                        pick_request.removeEventListener(this);
                        finish();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null)
        {

            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");
            orderID = getIntent().getStringExtra("orderID");

            //just copy getDirection from welcome activity
            getDirection(lat,lng);

        }
    }

    private void cancelBooking(String customerId) {

        Common.ordering = false;
        finish();
//        Toast.makeText(getApplicationContext(), "dddddddddd", Toast.LENGTH_LONG).show();
//        Token token = new Token(customerId);
//
//        Toast.makeText(getApplicationContext(), "cancelID::" + customerId, Toast.LENGTH_LONG).show();
//
//       // Notification notification = new Notification("Cancelado!!","Chofer Cancelo");
//       // Sender sender = new Sender(token.getToken(),notification);
//
//        Map<String,String> content=new HashMap<>();
//        content.put("title","Cancelado");
//        content.put("message","Chofer cancelo tu petición");
//        DataMessage dataMessage=new DataMessage(token.getToken(),content);
//        mFCMService.sendMessage(dataMessage)
//                .enqueue(new Callback<FCMResponse>() {
//                    @Override
//                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                        if (response.body().success == 1)
//                        {
//                            Toast.makeText(CustommerCall.this, "Cancelado", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<FCMResponse> call, Throwable t) {
//
//                    }
//                });
    }

    private void getDirection(String lat,String lng) {


        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("RAPICITY",requestApi); //print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //after get routes just get first element of routes
                                JSONObject object = routes.getJSONObject(0);

                                //after get first element we need get array with name "legs"
                                JSONArray legs = object.getJSONArray("legs");

                                //and get first element of legs array
                                JSONObject legsObject = legs.getJSONObject(0);

                                //now get distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                //get time
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                                //Get Address
                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);





                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustommerCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying())
           mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer!=null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
}
