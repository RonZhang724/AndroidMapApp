package com.example.androidmap;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlaceAutocompleteFragment placeAutoComplete;
    private String API_KEY = "AIzaSyCYjArGK8qyUKkNEICYzs_P4ZH4Xc2k0-Q";
    private Switch switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // The auto-complete search bar learned from this tutorial:
        // https://stackoverflow.com/questions/45107806/autocomplete-search-bar-in-google-maps
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
                LatLng selectedLL = place.getLatLng();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(selectedLL).title(place.getAddress().toString()));
                // The zooming animation learned from this tutorial:
                // https://developers.google.com/maps/documentation/android-sdk/views
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(selectedLL)      // Sets the center of the map to selected place
                        .zoom(17)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        // The switch button usage learned from this example:
        // https://www.viralandroid.com/2015/11/android-switch-button-example.html
        switchButton = (Switch) findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    /**
     * Search geocoding for the address entered by the user
     *
     */
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
