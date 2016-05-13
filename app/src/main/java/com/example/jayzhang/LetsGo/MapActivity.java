package com.example.jayzhang.LetsGo;

/**
 * Modified from
 * source from http://stackoverflow.com/questions/14710744/how-to-draw-road-directions-between-two-geocodes-in-android-google-map-v2
 */

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static int LOCATION_PERMISSION = 1;

    SharedPreferences allDestinations;
    HashMap<String, LinkedHashSet<String>> mRandomRestaurants;
    HashMap<String, LinkedHashSet<String>> mRandomParks;
    HashMap<String, LinkedHashSet<String>> mRandomMuseums;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double defaultLat = 38.9851198;
    private Double defaultLon = -76.9451202;
    private MapGenerator mMapGenerator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        allDestinations = getSharedPreferences(MainActivity.PREFS_NAME_BUSINESS, MainActivity.PREFS_MODE_BUSINESS);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Going on the trip!", Toast.LENGTH_SHORT).show();
                allDestinations.edit().clear().commit();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRandomRestaurants = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_RESTAURANTS);
        mRandomParks = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_PARKS);
        mRandomMuseums = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_MUSEUMS);

        if (mRandomRestaurants != null || mRandomParks != null || mRandomMuseums != null)
            populateAllDestination(mRandomMuseums, mRandomRestaurants, mRandomParks);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .addApi(LocationServices.API)
                                .build();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        else
            mGoogleApiClient.connect();

        if (mMapGenerator == null)
            mMapGenerator = new MapGenerator(MapActivity.this,
                                            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(),
                                            allDestinations,
                                            false);

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.refresh) {
            allDestinations.edit().clear().commit();
            if (mRandomRestaurants != null || mRandomParks != null || mRandomMuseums != null)
                populateAllDestination(mRandomMuseums, mRandomRestaurants, mRandomParks);

            if (mLastLocation == null)
                mMapGenerator.drawMarkersMap(defaultLat, defaultLon);
            else
                mMapGenerator.drawMarkersMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mGoogleApiClient.connect();
        } else {
            mMapGenerator.drawMarkersMap(defaultLat, defaultLon);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mMapGenerator.drawMarkersMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else
            mMapGenerator.drawMarkersMap(defaultLat, defaultLon);

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void populateAllDestination (HashMap<String, LinkedHashSet<String>> randomMuseums,
                                         HashMap<String, LinkedHashSet<String>> randomRestaurants,
                                         HashMap<String, LinkedHashSet<String>> randomParks) {

            if (randomMuseums.size() > 0) {
                int random = randomNumber(randomMuseums.size());
                String name = findBusinessName(randomMuseums, random);
                allDestinations.edit().putStringSet(name, randomMuseums.get(name)).commit();
            }

            if (randomRestaurants.size() > 0) {
                int random = randomNumber(randomRestaurants.size());
                String name = findBusinessName(randomRestaurants, random);
                allDestinations.edit().putStringSet(name, randomRestaurants.get(name)).commit();
            }

            if (randomParks.size() > 1) {
                int random = randomNumber(randomParks.size());
                String name = findBusinessName(randomParks, random);
                allDestinations.edit().putStringSet(name, randomParks.get(name)).commit();
            }
    }

    private int randomNumber (int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

    private String findBusinessName (HashMap<String, LinkedHashSet<String>> hashMap, int index) {
        String name = null;
        for (String key : hashMap.keySet()) {
            if (index >= 0) {
                name = key;
                index --;
            } else
                break;
        }
        return name;
    }

}
