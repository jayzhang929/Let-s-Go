package com.example.jayzhang.LetsGo;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.MapFragment;
import com.yelp.clientlib.entities.Business;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PlaceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static int LOCATION_PERMISSION = 1;
    private static double METER_TO_MILE_CONVERSION = 1609.34;

    String curBusinessName;
    ArrayList<String> curBusinessAddress;
    ArrayList<String> mCurrentBusinessLatLon;
    private GoogleApiClient mGoogleApiClient;
    private MapGenerator mMapGenerator;
    private String lat;
    private String lon;
    private Location mLastLocation;
    private Location mBusinessLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        setTitle("Place");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        curBusinessName = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_NAME);
        curBusinessAddress = intent.getStringArrayListExtra(MainActivity.CURRENT_BUSINESS_ADDRESS);
        Bitmap curBusinessImage = intent.getParcelableExtra(MainActivity.CURRENT_BUSINESS_IMAGE);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(curBusinessImage);

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(curBusinessName);

        TextView address = (TextView) findViewById(R.id.address);
        address.setText(curBusinessAddress.get(0) + ", " + curBusinessAddress.get(1) + ", " + curBusinessAddress.get(2));

        lat = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_LAT);
        lon = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_LON);

        mBusinessLocation = new Location("");
        mBusinessLocation.setLatitude(Double.parseDouble(lat));
        mBusinessLocation.setLongitude(Double.parseDouble(lon));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentBusinessLatLon = new ArrayList<String>();
                mCurrentBusinessLatLon.add(lat);
                mCurrentBusinessLatLon.add(lon);
                LinkedHashSet<String> hashSet = new LinkedHashSet<String>();
                hashSet.add(lat);
                hashSet.add(lon);

                MainActivity.selectedBusinesses = getSharedPreferences(MainActivity.PREFS_NAME_BUSINESS, MainActivity.PREFS_MODE_BUSINESS).edit();
                MainActivity.selectedBusinesses.putStringSet(curBusinessName, hashSet);
                MainActivity.selectedBusinesses.commit();
                Toast.makeText(PlaceActivity.this, curBusinessName + " has been saved to your route!", Toast.LENGTH_SHORT).show();
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        SharedPreferences startingLoc = getSharedPreferences(MainActivity.PREFS_STARTING_LOCATION, MainActivity.PREFS_MODE_STARTING_LOCATION);
        if (startingLoc.getString(MainActivity.CURRENT_STARTING_LOCATION, "current location").equals("current location")) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            else
                mGoogleApiClient.connect();
        } else {
            Location currentStartingPoint = new Location("Starting Location");
            currentStartingPoint.setLatitude(Double.parseDouble(startingLoc.getString(MainActivity.CURRENT_STARTING_LAT, "null")));
            currentStartingPoint.setLongitude(Double.parseDouble(startingLoc.getString(MainActivity.CURRENT_STARTING_LON, "null")));
            Log.d("mBusinessLocation: ", String.valueOf(mBusinessLocation.getLatitude()) + " " + String.valueOf(mBusinessLocation.getLongitude()));
            Log.d("current S P: ", startingLoc.getString(MainActivity.CURRENT_STARTING_LAT, "null") + " " + startingLoc.getString(MainActivity.CURRENT_STARTING_LON, "null"));

            if (currentStartingPoint != null) {
                // find distance
                populateDistance(currentStartingPoint);
            }
        }

        mMapGenerator = new MapGenerator(PlaceActivity.this,
                                                    ((MapFragment) getFragmentManager().findFragmentById(R.id.business_map)).getMap(),
                                                    null,
                                                    true);

        mMapGenerator.drawMarkersMap(Double.parseDouble(lat), Double.parseDouble(lon));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.place_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("mBusinessLocation: ", String.valueOf(mBusinessLocation.getLatitude()) + " " + String.valueOf(mBusinessLocation.getLongitude()));
        Log.d("current location: ", String.valueOf(mLastLocation.getLatitude()) + " " + String.valueOf(mLastLocation.getLongitude()));
        if (mLastLocation != null) {
            // find distance
            populateDistance(mLastLocation);
        }

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void populateDistance(Location startingLocation) {
        float distanceInMeters = mBusinessLocation.distanceTo(startingLocation);
        Log.d("distance: ", String.valueOf(distanceInMeters));
        double distanceInMiles = distanceInMeters / METER_TO_MILE_CONVERSION;
        TextView distanceTextView = (TextView) findViewById(R.id.distance);
        distanceTextView.setText(String.valueOf((int) distanceInMiles + 1) + " miles");
    }
}
