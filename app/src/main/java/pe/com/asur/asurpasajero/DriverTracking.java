package pe.com.asur.asurpasajero;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.Helper.DirectionJSONParser;
import pe.com.asur.asurpasajero.Model.DataMessage;
import pe.com.asur.asurpasajero.Model.FCMResponse;
import pe.com.asur.asurpasajero.Model.Token;
import pe.com.asur.asurpasajero.Remote.IFCMService;
import pe.com.asur.asurpasajero.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener{

    private GoogleMap mMap;

    String riderLat,riderLng;

    String customerId;
    String customerKey;


    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle riderMarker;
    private Marker driverMarker;

    private Polyline direction;

    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    Button btnStartTrip;

    Location pickupLocation;

    boolean send_arrive = false;
    boolean start_trip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent() != null)
        {
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customerId");
        }

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        pickupLocation = new Location(LocationManager.GPS_PROVIDER);

//        Toast.makeText(DriverTracking.this, Common.passengerID, Toast.LENGTH_LONG).show();

//        getcustomerKey(customerId);

        setUpLocation();


        btnStartTrip = (Button)findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnStartTrip.getText().equals("COMENZAR VIAJE"))
                {
                    sendDropOffNotification(customerId, "Processing");
//                    pickupLocation = Common.mLastLocation;
                    pickupLocation.setLatitude(Double.parseDouble(riderLat));
                    pickupLocation.setLongitude(Double.parseDouble(riderLng));
                    btnStartTrip.setText("FINALIZAR");
                    start_trip = true;

                    Common.current_orerder_state = "b";

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    final DatabaseReference order_table = db.getReference(Common.driver_tbl);
                    order_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("b");




                }
                else if (btnStartTrip.getText().equals("FINALIZAR"))
                {
                    calculateCashFee(pickupLocation,Common.mLastLocation);
                    start_trip = false;

                    Common.current_orerder_state = "r";

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    final DatabaseReference order_table = db.getReference(Common.driver_tbl);
                    order_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("r");


                }
            }
        });
    }

//    private void getcustomerKey(final String token) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                    if (token.equals(userSnapshot.child("token").getValue())) {
//                        customerKey = userSnapshot.getKey();
//                        break;
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                throw databaseError.toException();
//            }
//        });
//    }



    private void calculateCashFee(final Location pickupLocation, Location mLastLocation) {

//        DatabaseReference accept_ref = FirebaseDatabase.getInstance().getReference(Common.token_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("accept");
//        accept_ref.setValue("false");

        Common.acceptance = false;
        Common.ordering = false;

        send_arrive = false;

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+pickupLocation.getLatitude()+","+pickupLocation.getLongitude()+"&"+
                    "destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {

                                //Extract JSON
                                //Root object
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                //Get Distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");
                                //only take number from string to parse
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                                //Get Distance
                                JSONObject timeObject = legsObject.getJSONObject("duration");
                                String time_text = timeObject.getString("text");
                                //only take number from string to parse
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));


                                sendDropOffNotification(customerId, "Finished");

                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                final DatabaseReference current_order = db.getReference(Common.order_table);
                                current_order.child(Common.orderID).removeValue();

                                //Create new Activity
                                Intent intent = new Intent(DriverTracking.this,TripDetail.class);
                                intent.putExtra("start_address",legsObject.getString("start_address"));
                                intent.putExtra("end_address",legsObject.getString("end_address"));
                                intent.putExtra("time",String.valueOf(time_value));
                                intent.putExtra("distance",String.valueOf(distance_value));
                                intent.putExtra("total",Common.formulaPrice(distance_value,time_value));
                                intent.putExtra("location_start",String.format("%f,%f",pickupLocation.getLatitude(),pickupLocation.getLongitude()));
                                intent.putExtra("location_end",String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                                startActivity(intent);
                                finish();



                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setUpLocation() {

        if (checkPlayServices())
        {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.mapa_style_map)
            );

            if (!isSuccess)
                Log.e("ERROR","Map style load failed !!!");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

        mMap = googleMap;

        riderMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(riderLat),Double.parseDouble(riderLng)))
                .radius(50) // 50 => radius is 50m
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));

        //create geo fencing with radius is 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl));
//        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat),Double.parseDouble(riderLng)),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Because here we will need customer Id rider id to send notification
                //so we will pass it from previos activity (customerCall)
//                if(key.equals(customerKey)) {
//                    sendArrivedNotification(customerId);
//                    btnStartTrip.setEnabled(true);
//                }
                if(!send_arrive)
                    sendArrivedNotification(customerId, key);

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void sendArrivedNotification(String customerId, String driverID) {
        Token token = new Token(customerId);
        //we will send notification with title is arrived and body is this string
      //  Notification notification = new Notification("LLEGO",String.format("El conductor %s espera por ti",Common.currentUser.getName()));
      //  Sender sender = new Sender(token.getToken(),notification);

        Map<String,String> content=new HashMap<>();
        content.put("title","Llego");
        content.put("message",String.format("El conductor %s espera por ti",Common.currentUser.getName()));
        content.put("driverID", driverID);
        DataMessage dataMessage=new DataMessage(token.getToken(), content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Send Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DriverTracking.this, "Send Success", Toast.LENGTH_SHORT).show();
                    send_arrive = true;
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }
    private void sendDropOffNotification(String customerId, String currentOrderstatus) {
        Token token = new Token(customerId);
        //we will send notification with title is arrived and body is this string
       // Notification notification = new Notification("Bajar",customerId);//ESTE ES EL VALO RQUE LE ENVIO A FIREBASE DEL PROYECTO DE PASAJER
       // Sender sender = new Sender(token.getToken(),notification);

        Map<String,String> content=new HashMap<>();
//        content.put("title","Bajar");
//        content.put("message",customerId);
        content.put("title",currentOrderstatus);
        content.put("orderID", Common.orderID);
        DataMessage dataMessage=new DataMessage(token.getToken(), content);


        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed Sending", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation != null)
        {

            final double latitude = Common.mLastLocation.getLatitude();
            final double longitude = Common.mLastLocation.getLongitude();

            if (driverMarker != null)
                driverMarker.remove();
            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                    .title("yo")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));

            if (direction != null)
                direction.remove();  //remove old direction
            getDirection();

        }
        else
        {
            Log.d("ERROR","Cannot get your location");
        }
    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("RAPICITY",requestApi); //print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {

                                new ParserTask().execute(response.body().toString());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Por Favor Esperar");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jobject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jobject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jobject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points=null;
            PolylineOptions polylineOptions = null;

            for (int i=0;i<lists.size();i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for (int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);

                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);

                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}
