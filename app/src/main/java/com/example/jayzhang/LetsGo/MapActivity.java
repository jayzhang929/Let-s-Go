package com.example.jayzhang.LetsGo;

/**
 * Modified from
 * source from http://stackoverflow.com/questions/14710744/how-to-draw-road-directions-between-two-geocodes-in-android-google-map-v2
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;

import java.util.HashMap;
import java.util.LinkedHashSet;
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
    private SharedPreferences mInterests;
    private SharedPreferences mStartingLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setTitle("Map");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        allDestinations = getSharedPreferences(MainActivity.PREFS_NAME_BUSINESS, MainActivity.PREFS_MODE_BUSINESS);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Going on the trip!", Toast.LENGTH_SHORT).show();
                allDestinations.edit().clear().commit();
                Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="
                        + MapGenerator.mStartingLocation.latitude + ","
                        + MapGenerator.mStartingLocation.longitude + "&daddr="
                        + MapGenerator.mCurrentDestination.latitude + "," + MapGenerator.mCurrentDestination.longitude));

                navigation.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                startActivity(navigation);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRandomRestaurants = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_RESTAURANTS);
        mRandomParks = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_PARKS);
        mRandomMuseums = (HashMap<String, LinkedHashSet<String>>) getIntent().getSerializableExtra(MainActivity.RANDOM_MUSEUMS);
        mInterests = getSharedPreferences(MainActivity.PREFS_NAME_INTERESTS, MainActivity.PREFS_MODE_INTERESTS);

        if (mRandomRestaurants != null || mRandomParks != null || mRandomMuseums != null)
            populateAllDestination(mRandomMuseums, mRandomRestaurants, mRandomParks);

        if (mMapGenerator == null)
            mMapGenerator = new MapGenerator(MapActivity.this,
                    ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(),
                    allDestinations,
                    false);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .addApi(LocationServices.API)
                                .build();
        }

        mStartingLoc = getSharedPreferences(MainActivity.PREFS_STARTING_LOCATION, MainActivity.PREFS_MODE_STARTING_LOCATION);
        if (mStartingLoc.getString(MainActivity.CURRENT_STARTING_LOCATION, "current location").equals("current location")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            else
                mGoogleApiClient.connect();
        } else
            plotMarkers();

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        if (getIntent().getBooleanExtra(MainActivity.RANDOM_GENERATE_PAGE, false))
            getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.refresh) {
            plotMarkers();
            return true;
        }

        if (id == R.id.customize) {
            startCustimzationDialog();
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
        plotMarkers();

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

            if (randomMuseums != null && randomMuseums.size() > 0) {
                int random = randomNumber(randomMuseums.size());
                String name = findBusinessName(randomMuseums, random);
                allDestinations.edit().putStringSet(name, randomMuseums.get(name)).commit();
            }

            if (randomRestaurants != null && randomRestaurants.size() > 0) {
                int random = randomNumber(randomRestaurants.size());
                String name = findBusinessName(randomRestaurants, random);
                allDestinations.edit().putStringSet(name, randomRestaurants.get(name)).commit();
            }

            if (randomParks != null && randomParks.size() > 1) {
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

    private void startCustimzationDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.random_generate_customization, (ViewGroup) findViewById(R.id.dialogLayout));
        boolean likeHiking = mInterests.getBoolean("Park", false);
        boolean likeMuseum = mInterests.getBoolean("Museum", false);
        boolean likeRestaurant = mInterests.getBoolean("Restaurant", false);

        final CheckBox hikingCheckBox = (CheckBox) layout.findViewById(R.id.rdmPark);
        final CheckBox museumCheckBox = (CheckBox) layout.findViewById(R.id.rdmMuseum);
        final CheckBox restaurantCheckBox = (CheckBox) layout.findViewById(R.id.rdmRestaurant);

        if (likeHiking) hikingCheckBox.setChecked(true);
        if (likeMuseum) museumCheckBox.setChecked(true);
        if (likeRestaurant) restaurantCheckBox.setChecked(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInterests.edit().clear().commit();
                if (hikingCheckBox.isChecked())
                    mInterests.edit().putBoolean("Park", true).commit();
                if (museumCheckBox.isChecked())
                    mInterests.edit().putBoolean("Museum", true).commit();
                if (restaurantCheckBox.isChecked())
                    mInterests.edit().putBoolean("Restaurant", true).commit();

                plotMarkers();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void plotMarkers() {
        if (getIntent().getBooleanExtra(MainActivity.RANDOM_GENERATE_PAGE, false))
            allDestinations.edit().clear().commit();

        if (mRandomRestaurants != null || mRandomParks != null || mRandomMuseums != null) {
            HashMap<String, LinkedHashSet<String>> rdmRestaurants = mInterests.contains("Restaurant") ? mRandomRestaurants : null;
            HashMap<String, LinkedHashSet<String>> rdmMuseums = mInterests.contains("Museum") ? mRandomMuseums : null;
            HashMap<String, LinkedHashSet<String>> rdmParks = mInterests.contains("Park") ? mRandomParks : null;
            Log.d("mInterest, park", rdmParks == null ? "null" : "not null");
            populateAllDestination(rdmMuseums, rdmRestaurants, rdmParks);
        }

        if (mStartingLoc.getString(MainActivity.CURRENT_STARTING_LOCATION, "current location").equals("current location")) {
            if (mLastLocation == null)
                mMapGenerator.drawMarkersMap(defaultLat, defaultLon);
            else
                mMapGenerator.drawMarkersMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else {
            mMapGenerator.drawMarkersMap(Double.parseDouble(mStartingLoc.getString(MainActivity.CURRENT_STARTING_LAT, "null")),
                                         Double.parseDouble(mStartingLoc.getString(MainActivity.CURRENT_STARTING_LON, "null")));
        }
    }


}
