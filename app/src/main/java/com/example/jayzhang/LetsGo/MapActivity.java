package com.example.jayzhang.LetsGo;

/**
 * Modified from
 * source from http://stackoverflow.com/questions/14710744/how-to-draw-road-directions-between-two-geocodes-in-android-google-map-v2
 */

import android.*;
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
import com.yelp.clientlib.entities.Business;

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

    private GoogleMap googleMap;
    ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    SharedPreferences allDestinations;
    HashMap<String, LinkedHashSet<String>> mRandomRestaurants;
    HashMap<String, LinkedHashSet<String>> mRandomParks;
    HashMap<String, LinkedHashSet<String>> mRandomMuseums;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double defaultLat = 38.9851198;
    private Double defaultLon = -76.9451202;


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

        // drawMarkersMap();

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
                drawMarkersMap(defaultLat, defaultLon);
            else
                drawMarkersMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());

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
            drawMarkersMap(defaultLat, defaultLon);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            drawMarkersMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else
            drawMarkersMap(defaultLat, defaultLon);

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

    private void drawMarkersMap (Double startingLat, Double startingLon) {
        try {
            if (googleMap == null)
                googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            markerPoints.clear();
            googleMap.clear();

            // add marker
            // final LatLng loc = new LatLng(38.9851198, -76.9451202);
            final LatLng loc = new LatLng(startingLat, startingLon);
            markerPoints.add(loc);
            MarkerOptions optionLoc = new MarkerOptions().position(loc).title("Current Location").snippet("home")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            googleMap.addMarker(optionLoc);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 11));

            Map latLonMap = allDestinations.getAll();
            int markerPointIndex = 0;
            Log.d("latLonMap Size: ", String.valueOf(latLonMap.size()));
            for (Object location : latLonMap.keySet()) {
                Log.d("location: ", (String) location);
                String[] latlon = latLonMap.get(location).toString().split(",");
                Double lon = Double.parseDouble((latlon[1].split("\\]"))[0]);
                Double lat = Double.parseDouble((latlon[0].split("\\["))[1]);

                // double check for correct lat and lon
                if (lat < lon) {
                    Double temp = lat;
                    lat = lon;
                    lon = temp;
                }

                LatLng currentDestination = new LatLng(lat, lon);
                markerPoints.add(currentDestination);

                MarkerOptions optionDestination = new MarkerOptions()
                        .position(currentDestination)
                        .title((String) location)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                googleMap.addMarker(optionDestination);

                LatLng origin = markerPoints.get(markerPointIndex);
                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, currentDestination);
                Log.d("url: ", url);

                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);

                // currentDestination become the starting point of the next segment of route
                markerPointIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception downloading", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
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
