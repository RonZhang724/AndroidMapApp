package com.example.androidmap;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {


    private GoogleMap mMap;
    private String API_KEY = "AIzaSyCYjArGK8qyUKkNEICYzs_P4ZH4Xc2k0-Q";
    boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LatLng currentLL;
    private float tilt = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // The auto-complete search bar learned from this tutorial:
        // https://stackoverflow.com/questions/45107806/autocomplete-search-bar-in-google-maps
        PlaceAutocompleteFragment placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
                currentLL = place.getLatLng();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLL).title(place.getAddress().toString()));
                moveCamera(currentLL, 17, 0);
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        // The switch button usage learned from this example:
        // https://www.viralandroid.com/2015/11/android-switch-button-example.html
        Switch switchButton = (Switch) findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Switched to hybrid map", Toast.LENGTH_LONG).show();
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    Toast.makeText(getApplicationContext(), "Switched to normal map", Toast.LENGTH_LONG).show();
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        Switch switchButton2 = (Switch) findViewById(R.id.switch2);
        switchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Switched to 3D view", Toast.LENGTH_LONG).show();
                    tilt = 60;
                    moveCamera(currentLL, 17, 0);
                } else {
                    tilt = 0;
                    Toast.makeText(getApplicationContext(), "Switched to 2D view", Toast.LENGTH_LONG).show();
                    moveCamera(currentLL, 17, 0);
                }
            }
        });

        // Press a button to jump back home
        Button locateButton = findViewById(R.id.locate);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Going home", Toast.LENGTH_LONG).show();
                String address = "3110 Red River St";
                // Use Volley to make geocoding API request, learned from:
                // https://developer.android.com/training/volley/simple
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String requestURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address.replace(' ', '+') + "&key=" + API_KEY;
                Log.d("url: ", requestURL);
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject reader = new JSONObject(response);
                                    Log.d("Response is: ", reader.get("status").toString());
                                    double lat = (double) reader.getJSONArray("results").getJSONObject(0)
                                            .getJSONObject("geometry")
                                            .getJSONObject("location")
                                            .get("lat");
                                    double lng = (double) reader.getJSONArray("results").getJSONObject(0)
                                            .getJSONObject("geometry")
                                            .getJSONObject("location")
                                            .get("lng");
                                    String county = (String) reader.getJSONArray("results").getJSONObject(0)
                                            .get("formatted_address");
                                    currentLL = new LatLng(lat, lng);
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(currentLL).title(county));
                                    moveCamera(currentLL, 17, 0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error: ", "VolleyError");
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        });

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in Sydney and move the camera
        currentLL = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(currentLL).title("Marker in Sydney"));
        moveCamera(currentLL, 17, 0);

        getLocationPermission();
    }

    private void moveCamera(LatLng latLng, float zoom, float bearing){
        // The zooming animation learned from this tutorial:
        // https://developers.google.com/maps/documentation/android-sdk/views
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to selected place
                .zoom(zoom)                   // Sets the zoom
                .bearing(bearing)                // Sets the orientation of the camera to east
                .tilt(tilt)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // Use fused location client to get the device location
    // https://developers.google.com/maps/documentation/android-sdk/location
    // https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location access granted", Toast.LENGTH_SHORT).show();
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        } else {
            Toast.makeText(this, "Need Location access", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            double lat = mLastKnownLocation.getLatitude();
                            double lng = mLastKnownLocation.getLongitude();
                            currentLL = new LatLng(lat, lng);
                            //mMap.clear();
                            //mMap.addMarker(new MarkerOptions().position(selectedLL).title("current location"));
                            moveCamera(currentLL, 17, 0);
                        } else {
                            Toast.makeText(getApplicationContext(), "Current location is null. Using defaults.", Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "Exception: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Go to current location", Toast.LENGTH_SHORT).show();
        getDeviceLocation();
        return false;
    }


//    public void onSearch(View view){
//        // Minimize the keyboard first, learned from:
//        // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
//        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//
//        // The user input and button action learned from this tutorial:
//        // https://developer.android.com/training/basics/firstapp/starting-activity
//        EditText editText = (EditText) findViewById(R.id.editText);
//        String address = editText.getText().toString();
//        Log.d("user input", address);
//
//        // Use Volley to make geocoding API request, learned from:
//        // https://developer.android.com/training/volley/simple
//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String requestURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address.replace(' ', '+') + "&key=" + API_KEY;
//        Log.d("url: ", requestURL);
//        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject reader = new JSONObject(response);
//                            Log.d("Response is: ", reader.get("status").toString());
//                            double lat = (double) reader.getJSONArray("results").getJSONObject(0)
//                                    .getJSONObject("geometry")
//                                    .getJSONObject("location")
//                                    .get("lat");
//                            double lng = (double) reader.getJSONArray("results").getJSONObject(0)
//                                    .getJSONObject("geometry")
//                                    .getJSONObject("location")
//                                    .get("lng");
//                            String county = (String) reader.getJSONArray("results").getJSONObject(0)
//                                    .get("formatted_address");
//                            LatLng selectedLL = new LatLng(lat, lng);
//                            mMap.clear();
//                            mMap.addMarker(new MarkerOptions().position(selectedLL).title(county));
//                            // The zooming animation learned from this tutorial:
//                            // https://developers.google.com/maps/documentation/android-sdk/views
//                            CameraPosition cameraPosition = new CameraPosition.Builder()
//                                    .target(selectedLL)      // Sets the center of the map to selected place
//                                    .zoom(17)                   // Sets the zoom
//                                    .bearing(0)                // Sets the orientation of the camera to east
//                                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                                    .build();                   // Creates a CameraPosition from the builder
//                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("Error: ", "VolleyError");
//                }
//            });
//
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
//    }
}
