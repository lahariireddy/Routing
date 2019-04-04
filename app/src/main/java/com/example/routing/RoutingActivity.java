package com.example.routing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.routing.RoutingHelpers.FetchURL;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


import com.example.routing.RoutingHelpers.TaskLoadedCallback;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;



public class RoutingActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    double latitude, longitude;
    GoogleMap mMap;
    Marker sourceMarker = null, destMarker = null;
    Button getDirection;
    private Polyline currentPolyline;
    EditText source_textBox, dest_textBox;
    Button source_button, dest_button;
    HashMap<String,MarkerOptions> hashMapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        getDirection = findViewById(R.id.btnGetDirection);
        source_textBox = findViewById(R.id.source_location);
        dest_textBox = findViewById(R.id.destination_location);
        source_button = findViewById(R.id.source_button);
        dest_button = findViewById(R.id.destination_button);
        hashMapMarker = new HashMap<>();

        //Get starting point and add marker
        //Current location by default
        source_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = source_textBox.getText().toString();

                LatLng latLng = null;
                try {
                    if(location!="") {
                        latLng = getLatLng(location);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(latLng!=null) {

                    if(sourceMarker != null){
                        mMap.clear();
                        hashMapMarker.remove("source");
                        addAllMarkers();
                        sourceMarker = null;

                    }

                    if(sourceMarker == null) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.draggable(true);
                        try {
                            markerOptions.title(getAddress(latLng.latitude, latLng.longitude));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        sourceMarker = mMap.addMarker(markerOptions);
                        hashMapMarker.put("source", markerOptions);
                        mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }



            }
        });

        //Clear the textbox
        source_textBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source_textBox.setText("");
            }
        });

        //Clear destination textbox
        dest_textBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dest_textBox.setText("");
            }
        });


        //Get desitnation and add marker
        dest_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = dest_textBox.getText().toString();

                LatLng latLng = null;
                try {
                    if(location!="") {
                        latLng = getLatLng(location);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(latLng!=null) {

                    if(destMarker != null){
                        mMap.clear();
                        hashMapMarker.remove("destination");
                        addAllMarkers();
                        sourceMarker = null;

                    }

                    if(destMarker == null) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.draggable(true);
                        try {
                            markerOptions.title(getAddress(latLng.latitude, latLng.longitude));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        destMarker = mMap.addMarker(markerOptions);
                        hashMapMarker.put("destination", markerOptions);
                        mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }



            }
        });



        //OnClick 'Get Directions' button
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentPolyline != null) {
                    currentPolyline.remove();
                    mMap.clear();
                }

                getSourceDestMarker();

                if (sourceMarker != null && destMarker != null) {
                    new FetchURL(RoutingActivity.this).execute(getUrl(sourceMarker.getPosition(), destMarker.getPosition(), "driving"), "driving");
                }
                else if(sourceMarker == null && destMarker!=null){
                    new FetchURL(RoutingActivity.this).execute(getUrl(mCurrLocationMarker.getPosition(), destMarker.getPosition(), "driving"), "driving");

                }
                else{
                    Toast.makeText(RoutingActivity.this,"Enter desination", Toast.LENGTH_LONG).show();
                }

            }
        });



        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);

    }

    //Executed if source and dest is entered, but searchs aren't pressed
    public void getSourceDestMarker() {
        //Get Source Marker
        String location = source_textBox.getText().toString();

        LatLng latLng = null;
        try {
            if (location != "") {
                latLng = getLatLng(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (latLng != null) {

            if (sourceMarker != null) {
                mMap.clear();
                hashMapMarker.remove("source");
                addAllMarkers();
                sourceMarker = null;

            }

            if (sourceMarker == null) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.draggable(true);
                try {
                    markerOptions.title(getAddress(latLng.latitude, latLng.longitude));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                sourceMarker = mMap.addMarker(markerOptions);
                hashMapMarker.put("source", markerOptions);
                mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


            }
        }

        //get dest marker
        location = dest_textBox.getText().toString();

        latLng = null;
        try {
            if(location!="") {
                latLng = getLatLng(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(latLng!=null) {

            if(destMarker != null){
                mMap.clear();
                hashMapMarker.remove("destination");
                addAllMarkers();
                sourceMarker = null;

            }

            if(destMarker == null) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.draggable(true);
                try {
                    markerOptions.title(getAddress(latLng.latitude, latLng.longitude));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                destMarker = mMap.addMarker(markerOptions);
                hashMapMarker.put("destination", markerOptions);
                mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }


    }

    //Adds curr, source and dest markers
    public void addAllMarkers(){
        if(hashMapMarker.get("source")!=null){
            mMap.addMarker(hashMapMarker.get("source"));
        }
        if(hashMapMarker.get("destination")!=null){
            mMap.addMarker(hashMapMarker.get("destination"));
        }
        if(hashMapMarker.get("current")!=null){
            mMap.addMarker(hashMapMarker.get("current"));
        }

    }

    //Convert string to lat, lng
    public LatLng getLatLng(String location) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        addresses = geocoder.getFromLocationName(location, 1);
        if(addresses.size() > 0) {
            double resLat = addresses.get(0).getLatitude();
            double reslng = addresses.get(0).getLongitude();

            LatLng resLatLng = new LatLng(resLat, reslng);


            return resLatLng;
        }
        return null;

    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    //Get URL for getting data from Directions API
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        hashMapMarker.put("current",markerOptions);
        //sourceMarker = mCurrLocationMarker;

        //Set source_textBox to current location address
        try {
            source_textBox.setText(getAddress(latitude, longitude));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


        Toast.makeText(RoutingActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }

    }

    //Convert lat, lng coordinate to address
    public String getAddress(double lat, double lng) throws IOException {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        return address;

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
