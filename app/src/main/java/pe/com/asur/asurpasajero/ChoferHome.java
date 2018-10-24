package pe.com.asur.asurpasajero;

import android.*;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.Model.Token;
import pe.com.asur.asurpasajero.Model.User;
import pe.com.asur.asurpasajero.Remote.IGoogleAPI;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pe.com.asur.asurpasajero.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChoferHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback

{

    RelativeLayout rootLayout;

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //Car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng starPostion, endPostion, currentPosition;
    private int index, next;
    // private Button btnGo;
    private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;

    private IGoogleAPI mService;

    //presense system
    DatabaseReference onlineRef, currentUserRef;

    //FIrebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {
                starPostion = polyLineList.get(index);
                endPostion = polyLineList.get(next);
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPostion.longitude + (1 - v) * starPostion.longitude;
                    lat = v * endPostion.latitude + (1 - v) * starPostion.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(starPostion, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);

        }
    };

    private float getBearing(LatLng starPostion, LatLng endPostion) {
        double lat = Math.abs(starPostion.latitude - endPostion.latitude);
        double lng = Math.abs(starPostion.longitude - endPostion.longitude);

        if (starPostion.latitude < endPostion.latitude && starPostion.longitude < endPostion.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (starPostion.latitude >= endPostion.latitude && starPostion.longitude < endPostion.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (starPostion.latitude >= endPostion.latitude && starPostion.longitude >= endPostion.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (starPostion.latitude < endPostion.latitude && starPostion.longitude >= endPostion.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chofer_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigatopmHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView) navigatopmHeaderView.findViewById(R.id.txtDriverName);
        TextView txtStarts = (TextView) navigatopmHeaderView.findViewById(R.id.txtStars);
        CircleImageView imageAvatar = (CircleImageView) navigatopmHeaderView.findViewById(R.id.image_avatar);


        if (Common.currentUser.getName() != null && !TextUtils.isEmpty(Common.currentUser.getName())) {
            txtName.setText(Common.currentUser.getName().toString());
        }


        if (Common.currentUser.getRates() != null && !TextUtils.isEmpty(Common.currentUser.getRates())) {
            txtStarts.setText(Common.currentUser.getRates().toString());
        }

        if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        } else {
            Picasso.with(this)
                    .load(R.mipmap.ic_launcher_round)
                    .into(imageAvatar);

        }

        //paste here
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //presense system
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //we will remove value from driver tbl when driver disconnected
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Init View
//        if(getIntent() != null) {
//            String state = getIntent().getStringExtra("state");
//            if(state != null)
//                if (state.equals("cancel"))
//                    setOnline();
//
//        }

        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {


//                    FirebaseDatabase db = FirebaseDatabase.getInstance();
//                    final DatabaseReference tokens = db.getReference(Common.driver_tbl);
//
//
//                    tokens.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Toast.makeText(getApplicationContext(), "hahahahahad", Toast.LENGTH_LONG).show();
//                            if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                Snackbar.make(rootLayout,"Failed to Login. Please login other user.", Snackbar.LENGTH_SHORT)
//                                        .show();
//                                signout();
//                                Toast.makeText(getApplicationContext(), "has child", Toast.LENGTH_LONG).show();
//
//                            } else {
//                                setOnline();
//                                Toast.makeText(getApplicationContext(), "hasn't child", Toast.LENGTH_LONG).show();
//                            }
//
////
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });






                    setOnline();




//                    Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
//                    FirebaseDatabase.getInstance().goOnline(); //set connected when switch to  on
//
//                    if (ActivityCompat.checkSelfPermission(ChoferHome.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChoferHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                        return;
//                    }
//                    buildLocationCallBack();
//                    buildLocationRequest();
//                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
//                    displayLocation();
//                    Snackbar.make(mapFragment.getView(), "en line", Snackbar.LENGTH_SHORT)
//                            .show();
                } else {
                    FirebaseDatabase.getInstance().goOffline(); //set disconnect when switch ot off

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    mCurrent.remove();
                    mMap.clear();
                    if (handler != null)
                        handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(), "fuera de linea", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });

        polyLineList = new ArrayList<>();


        //places API
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked()) {
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");

                    getDirection();
                } else {
                    Toast.makeText(ChoferHome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(Status status) {
                Toast.makeText(ChoferHome.this, "" + status.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        //Geo Fire
        drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        geoFire = new GeoFire(drivers);

        setUpLocation();

        mService = Common.getGoogleAPI();

        updateFirebaseToken();



    }

    private void setOnline() {

        updateFirebaseToken();

        FirebaseDatabase.getInstance().goOnline(); //set connected when switch to  on

//        order_table.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if(!dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
////                    Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
////                    Token token = new Token(FirebaseInstanceId.getInstance().getToken());
////                    tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
//                    order_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("r");
//                    Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        if (ActivityCompat.checkSelfPermission(ChoferHome.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChoferHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
        displayLocation();
        Snackbar.make(mapFragment.getView(), "en line", Snackbar.LENGTH_SHORT)
                .show();


    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference(Common.token_tbl);

        tokens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

//                if(!dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
////                    Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
//                    Token token = new Token(FirebaseInstanceId.getInstance().getToken());
//                    tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                            .setValue(token);
////                    tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("r");
//                }
//                    Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
                    Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                    tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(token);
//                    tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("r");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
//        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .setValue(token);
//        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("accept").setValue("false");
    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("RAPICITY", requestApi); //print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }
                                //Adjusting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng : polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size() - 1))
                                        .title("Pickup Location"));

                                //Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int) valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable, 3000);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(ChoferHome.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallBack();
                    buildLocationRequest();
                    if (location_switch.isChecked())
                        displayLocation();

                }
        }

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            buildLocationRequest();
            buildLocationCallBack();
            if (location_switch.isChecked())
                displayLocation();

        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };

    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {
                    if (location_switch.isChecked()) {
                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longitude = Common.mLastLocation.getLongitude();

                        LatLng center = new LatLng(latitude, longitude);
                        LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                        LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                        LatLngBounds bounds = LatLngBounds.builder()
                                .include(northSide)
                                .include(southSide)
                                .build();

                        places.setBoundsBias(bounds);
                        places.setFilter(typeFilter);


                        //update to firebase
                            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    //add Marker
                                    if (mCurrent != null)
                                        mCurrent.remove(); //remove already marker
                                    mCurrent = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude, longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                            .title("yo Location"));

                                    //move camera to this postion
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

                                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                                    final DatabaseReference driverRef = db.getReference(Common.driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                    driverRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("r");
//                                    order_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("id").setValue(45);
                                    driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.hasChild("status"))
                                                driverRef.child("status").setValue(Common.current_orerder_state);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });

                    }
                } else {
                    Log.d("ERROR", "Cannot get your location");
                }
            }
        });


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chofer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.current_order) {

            final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            final DatabaseReference current_order = db.getReference(Common.register_current_order);

            current_order.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        if (key.equals(orderSnapshot.child("driverID").getValue())) {
                           final String passengerKey = orderSnapshot.getKey();

                            final String lat = orderSnapshot.child("lat").getValue().toString();
                            final String lng = orderSnapshot.child("lng").getValue().toString();

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        if (passengerKey.equals(userSnapshot.getKey())) {
                                            String passengerID = userSnapshot.child("token").getValue().toString();


                                            Intent intent = new Intent(ChoferHome.this, DriverTracking.class);
                                            intent.putExtra("customerId", passengerID);
                                            intent.putExtra("lat", lat);
                                            intent.putExtra("lng", lng);
                                            startActivity(intent);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    throw databaseError.toException();
                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });


        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_sign_out) {
            signout();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_change_pwd) {
            ShowDialogChangePwd();
        } else if (id == R.id.nav_update_info) {
            showUpdateInfo();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChoferHome.this);
        alertDialog.setTitle("Actualizar Perfil");
        alertDialog.setMessage("Por Favor ingrese la información");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_info, null);

        final MaterialEditText edtName = (MaterialEditText) layout_pwd.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) layout_pwd.findViewById(R.id.edtPhone);
        final ImageView image_upload = (ImageView) layout_pwd.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_pwd);


        //set Button
        alertDialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(ChoferHome.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();
                Map<String, Object> updateInfo = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateInfo.put("name", name);

                if (!TextUtils.isEmpty(phone))
                    updateInfo.put("phone", phone);


                DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChoferHome.this, "Perfil Actualizado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChoferHome.this, "Error al Actualizar Perfil", Toast.LENGTH_SHORT).show();
                                }

                                waitingDialog.dismiss();
                            }
                        });


            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Subiendo....");
                mDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mDialog.dismiss();

                                // @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //Update this url to avatar property of user
                                        //first you nedd avvatar property en user model
                                        Map<String, Object> avatarUrl = new HashMap<>();
                                        avatarUrl.put("avatarUrl", uri.toString());
                                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                                        driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(avatarUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ChoferHome.this, "Subido correctamente", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ChoferHome.this, "Upload Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });


                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @SuppressWarnings("VisibleForTests")
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Subiendo: " + progress + "%");
                            }
                        });

            }

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Seleccione Imagen"), Common.PICK_IMAGE_REQUEST);


    }

    private void ShowDialogChangePwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChoferHome.this);
        alertDialog.setTitle("Cambiar Contraseña");
        alertDialog.setMessage("Por Favor ingrese la información");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);
        alertDialog.setView(layout_pwd);
        //set buttons
        alertDialog.setPositiveButton("Cambiar Contraseña", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                final android.app.AlertDialog waitingDialog = new SpotsDialog(ChoferHome.this);
                waitingDialog.show();
                if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    //get auth credentials from user for re-autentication
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Map<String, Object> password = new HashMap<>();
                                                            password.put("password", edtRepeatPassword.getText().toString());
                                                            DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                                                            driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(ChoferHome.this, "Contraseña cambiada", Toast.LENGTH_SHORT).show();

                                                                            else
                                                                                Toast.makeText(ChoferHome.this, "Error al cambiar Contraseña", Toast.LENGTH_SHORT).show();

                                                                            waitingDialog.dismiss();
                                                                        }
                                                                    });
                                                        } else {
                                                            // Toast.makeText(ChoferHome.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                            Toast.makeText(ChoferHome.this, "Cantidad de caracteres mínimos 6", Toast.LENGTH_SHORT).show();
                                                            waitingDialog.dismiss();
                                                        }
                                                    }
                                                });
                                    } else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(ChoferHome.this, "Error password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(ChoferHome.this, "password Incorrecto", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void signout() {

//        ////remove token
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        DatabaseReference token_table = db.getReference(Common.token_tbl);
//        token_table.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

        Paper.init(this);
        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        FirebaseDatabase.getInstance().goOffline(); //set disconnect when switch ot off


        Intent intent = new Intent(ChoferHome.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.mapa_style_map)
            );

            if (!isSuccess)
                Log.e("ERROR", "Map style load failed !!!");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Update location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

    }
}
