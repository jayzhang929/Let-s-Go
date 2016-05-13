package com.example.jayzhang.LetsGo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.List;
import java.util.Map;

/**
 * Created by jayzhang on 5/13/16.
 */
public class MapGenerator {

    Activity mActivity;
    SharedPreferences allDestinations;
    private GoogleMap googleMap;
    ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    Boolean mSingleBusinessMap;

    public MapGenerator(Activity activity, GoogleMap googleMap, SharedPreferences allDestinations, Boolean singleBusinessMap) {
        mActivity = activity;
        this.googleMap = googleMap;
        this.allDestinations = allDestinations;
        mSingleBusinessMap = singleBusinessMap;
    }

    public void drawMarkersMap (Double startingLat, Double startingLon) {
        try {

            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            markerPoints.clear();
            googleMap.clear();

            // add marker
            final LatLng loc = new LatLng(startingLat, startingLon);
            markerPoints.add(loc);
            MarkerOptions optionLoc = new MarkerOptions().position(loc);
            googleMap.addMarker(optionLoc);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 11));

            if (!mSingleBusinessMap) {

                optionLoc.title("Current Location").snippet("home")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

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
            } else {
                optionLoc.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
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

}
