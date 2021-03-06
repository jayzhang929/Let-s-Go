package com.example.jayzhang.LetsGo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import retrofit.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener, AdapterView.OnItemSelectedListener {

    public final static String CURRENT_BUSINESS_ADDRESS = "com.example.jayzhang.LetsGo.BUSINESS_ADDRESS";
    public final static String CURRENT_BUSINESS_IMAGE = "com.example.jayzhang.LetsGo.BUSINESS_IMAGE";
    public final static String CURRENT_BUSINESS_NAME = "com.example.jayzhang.LetsGo.BUSINESS_NAME";
    public final static String CURRENT_BUSINESS_RATING = "com.example.jayzhang.LetsGo.BUSINESS_RATING";
    public final static String CURRENT_BUSINESS_DISTANCE = "com.example.jayzhang.LetsGo.BUSINESS_DISTANCE";
    public final static String CURRENT_BUSINESS_LAT = "com.example.jayzhang.LetsGo.BUSINESS_LAT";
    public final static String CURRENT_BUSINESS_LON = "com.example.jayzhang.LetsGo.BUSINESS_LON";
    public final static String RANDOM_RESTAURANTS = "com.example.jayzhang.LetsGo.RANDOM_RESTAURANTS";
    public final static String RANDOM_PARKS = "com.example.jayzhang.LetsGo.RANDOM_PARKS";
    public final static String RANDOM_MUSEUMS = "com.example.jayzhang.LetsGo.RANDOM_MUSEUMS";
    public final static String RANDOM_GENERATE_PAGE = "com.example.jayzhang.LetsGo.RANDOM_GENERATE_PAGE";
    static final String CURRENT_PLACE = "currentPlace";
    static final String CURRENT_TOPIC = "currentTopic";
    public static final String CURRENT_STARTING_LOCATION = "currentStartingLocation";
    public static final String CURRENT_STARTING_LAT = "currentStartingLat";
    public static final String CURRENT_STARTING_LON = "currentStartingLon";
    public final String PREFS_NAME = "SharedPrefs";
    public final int PREFS_MODE = 0;
    public static final String PREFS_NAME_BUSINESS = "selectedBusiness";
    public static final int PREFS_MODE_BUSINESS = 1;
    public static final String PREFS_NAME_INTERESTS = "selectedInterests";
    public static final int PREFS_MODE_INTERESTS = 2;
    public static final String PREFS_STARTING_LOCATION = "selectedStartingLocation";
    public static final int PREFS_MODE_STARTING_LOCATION = 3;

    private final static String consumerKey = "2q_fORhYgW2bMulCBkVOsw";
    private final static String consumerSecret = "5Xf4mXoItuhF66E373fXLFie1zI";
    private final static String token = "veqUNoadchDOaDdbHd_gUhfJXJX1r9GO";
    private final static String tokenSecret = "ZUZ88amNmp25m_-5oyqY6iTfyzU";

    private SearchView searchView;
    private ProgressDialog mProgressDialog;
    private PlaceAdapter mPlaceAdapter;
    private String mCurrentPlace;
    private String mCurrentTopic;
    private SharedPreferences.Editor editor;
    public static SharedPreferences.Editor selectedBusinesses;
    private HashMap<String, LinkedHashSet<String>> mRandomRestaurants;
    private HashMap<String, LinkedHashSet<String>> mRandomMuseums;
    private HashMap<String, LinkedHashSet<String>> mRandomParks;
    private Spinner mSpinner;
    private int callOnItemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSpinner = (Spinner) findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Wait while Let's Go find your destination :)");

        mCurrentPlace = "College Park, MD";
        mCurrentTopic = "parks";

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
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, PREFS_MODE);
        mCurrentPlace = sharedPreferences.getString(CURRENT_PLACE, "College Park, MD");
        mCurrentTopic = sharedPreferences.getString(CURRENT_TOPIC, "parks");

        callOnItemSelected = 0;
        mSpinner.setSelection(findIndexInCategoryArray(mCurrentTopic));

        yelpAsyncSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, PREFS_MODE);
        editor = sharedPreferences.edit();
        editor.putString(CURRENT_PLACE, mCurrentPlace);
        editor.putString(CURRENT_TOPIC, mCurrentTopic);

        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = new SearchView(this);
        menu.findItem(R.id.search).setActionView(searchView);
        // menu.findItem(R.id.search).setIcon(R.drawable.ic_search);
        // searchView.setBackgroundColor(getResources().getColor(R.color.white, null));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setIconified(true);
        searchView.setFocusable(true);
        searchView.setQueryHint("Let's Go to ...");

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_clear_route) {
            getSharedPreferences(PREFS_NAME_BUSINESS, PREFS_MODE_BUSINESS).edit().clear().commit();
        } else if (id == R.id.nav_random_generate) {
            mProgressDialog.show();
            BusinessesRandomGenerator businessesRandomGenerator = new BusinessesRandomGenerator();
            String[] params = {mCurrentPlace, null};
            businessesRandomGenerator.execute(params);
        } else if (id == R.id.starting_location_item) {
            startingLocationCustomizationDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (callOnItemSelected > 0) {
            mCurrentTopic = parent.getItemAtPosition(position).toString();

            Log.d("mCurrentTopic: ", mCurrentTopic);
            yelpAsyncSearch();
        }
        callOnItemSelected++;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void yelpAsyncSearch() {
        TextView textView = (TextView) findViewById(R.id.placeName);
        textView.setText(mCurrentPlace);
        mProgressDialog.show();

        String[] params = {mCurrentPlace, mCurrentTopic};
        YelpSearch yelpSearch = new YelpSearch();
        yelpSearch.execute(params);
    }

    private class YelpSearch extends AsyncTask<String, Void, ArrayList<Business>> {

        @Override
        protected ArrayList<Business> doInBackground(String... params) {
            return yelpSearch(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Business> businesses) {
            super.onPostExecute(businesses);
            // Log.d("businesses: ", businesses.toString());
            if (businesses == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Undefined Location");
                alertDialog.setMessage("Searching location is not defined. \n Please try again");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }

            CreatePlaceAdapter createPlaceAdapter = new CreatePlaceAdapter();
            createPlaceAdapter.execute(businesses);
        }
    }

    private class CreatePlaceAdapter extends AsyncTask<ArrayList<Business>, Void, PlaceAdapter> {

        @Override
        protected PlaceAdapter doInBackground(ArrayList<Business>... params) {
            return createPlaceAdapter(params[0]);
        }

        @Override
        protected void onPostExecute(PlaceAdapter placeAdapter) {
            mPlaceAdapter = placeAdapter;
            createGridView();
        }
    }

    // yelp search restaurants first
    private class BusinessesRandomGenerator extends AsyncTask<String, Void, ArrayList<Business>> {

        @Override
        protected ArrayList<Business> doInBackground(String... params) {
            return yelpSearch(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Business> businesses) {
            // clear current route first
            getSharedPreferences(PREFS_NAME_BUSINESS, PREFS_MODE_BUSINESS).edit().clear().commit();

            mRandomRestaurants = new HashMap<>();
            for (Business business : businesses) {
                LinkedHashSet<String> latLonLst = new LinkedHashSet<>();
                latLonLst.add(String.valueOf(business.location().coordinate().latitude()));
                latLonLst.add(String.valueOf(business.location().coordinate().longitude()));
                mRandomRestaurants.put(business.name(), latLonLst);
            }

            String[] params = {mCurrentPlace, "museums"};
            MuseumsRandomGenerator museumsRandomGenerator = new MuseumsRandomGenerator();
            museumsRandomGenerator.execute(params);
        }
    }

    private class MuseumsRandomGenerator extends AsyncTask<String, Void, ArrayList<Business>> {

        @Override
        protected ArrayList<Business> doInBackground(String... params) {
            return yelpSearch(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Business> businesses) {
            mRandomMuseums = new HashMap<>();
            for (Business business : businesses) {
                LinkedHashSet<String> latLonLst = new LinkedHashSet<>();
                latLonLst.add(String.valueOf(business.location().coordinate().latitude()));
                latLonLst.add(String.valueOf(business.location().coordinate().longitude()));
                mRandomMuseums.put(business.name(), latLonLst);
            }

            String[] params = {mCurrentPlace, "parks"};
            ParksRandomGenerator parksRandomGenerator = new ParksRandomGenerator();
            parksRandomGenerator.execute(params);

        }
    }

    private class ParksRandomGenerator extends AsyncTask<String, Void, ArrayList<Business>> {

        @Override
        protected ArrayList<Business> doInBackground(String... params) {
            return yelpSearch(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Business> businesses) {
            mRandomParks = new HashMap<>();

            for (Business business : businesses) {
                Log.d("category: ", business.categories().toString());
                LinkedHashSet<String> latLonLst = new LinkedHashSet<>();
                latLonLst.add(String.valueOf(business.location().coordinate().latitude()));
                latLonLst.add(String.valueOf(business.location().coordinate().longitude()));
                mRandomParks.put(business.name(), latLonLst);
            }

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra(RANDOM_RESTAURANTS, mRandomRestaurants);
            intent.putExtra(RANDOM_PARKS, mRandomParks);
            intent.putExtra(RANDOM_MUSEUMS, mRandomMuseums);
            intent.putExtra(RANDOM_GENERATE_PAGE, true);
            mProgressDialog.hide();
            startActivity(intent);
        }
    }


    private ArrayList<Business> yelpSearch(String location, String topic) {

        YelpAPIFactory apiFactory = new YelpAPIFactory(consumerKey, consumerSecret, token, tokenSecret);
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        if (topic != null) {
            params.put("term", topic);
            params.put("sort", "2");
        }
        // params.put("category-filter", "hiking");
        params.put("limit", "20");
        params.put("lang", "fr");


        ArrayList<Business> businesses = null;
        retrofit.Call<SearchResponse> call = yelpAPI.search(location, params);
        try {
            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();

            businesses = searchResponse.businesses();

        } catch (IOException e) {

        }

        return businesses;

    }

    private PlaceAdapter createPlaceAdapter(ArrayList<Business> businesses) {
        if (businesses == null)
            return null;

        PlaceAdapter placeAdapter = new PlaceAdapter(this);
        ArrayList<Bitmap> imageDrawables = new ArrayList<Bitmap>();
        ArrayList<Bitmap> ratingDrawables = new ArrayList<Bitmap>();

        try {
            for (Business b : businesses) {
                imageDrawables.add(PlaceAdapter.bitmapFromUrl(b.imageUrl()));
                ratingDrawables.add(PlaceAdapter.bitmapFromUrl(b.ratingImgUrl()));
            }
        } catch (IOException e) {

        }

        placeAdapter.setBusinesses(businesses);
        placeAdapter.setPlaceImages(imageDrawables);
        placeAdapter.setPlaceRatings(ratingDrawables);

        return placeAdapter;
    }

    private void createGridView(){
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(mPlaceAdapter);
        mProgressDialog.hide();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlaceActivity.class);
                intent.putExtra(CURRENT_BUSINESS_NAME, mPlaceAdapter.getBusiness(position).name());
                intent.putExtra(CURRENT_BUSINESS_ADDRESS, mPlaceAdapter.getBusiness(position).location().displayAddress());
                intent.putExtra(CURRENT_BUSINESS_IMAGE, mPlaceAdapter.getPlaceImage(position));
                intent.putExtra(CURRENT_BUSINESS_RATING, mPlaceAdapter.getPlaceRating(position));
                intent.putExtra(CURRENT_BUSINESS_DISTANCE, mPlaceAdapter.getBusiness(position).distance());
                intent.putExtra(CURRENT_BUSINESS_LAT, String.valueOf(mPlaceAdapter.getBusiness(position).location().coordinate().latitude()));
                intent.putExtra(CURRENT_BUSINESS_LON, String.valueOf(mPlaceAdapter.getBusiness(position).location().coordinate().longitude()));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mCurrentPlace = new String(query);
        searchView.clearFocus();
        yelpAsyncSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    private int findIndexInCategoryArray(String category) {
        int index = 0;
        switch (category) {
            case "Museums":
                index = 1;
                break;
            case "Parks":
                index = 2;
                break;
            case "Restaurants":
                index = 3;
                break;
            case "Festival":
                index = 4;
                break;
        }
        return index;
    }

    private void startingLocationCustomizationDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.starting_location_customization, (ViewGroup) findViewById(R.id.starting_location_dialog));

        final EditText editText = (EditText) view.findViewById(R.id.default_starting_location);

        final SharedPreferences startingLoc = getSharedPreferences(PREFS_STARTING_LOCATION, PREFS_MODE_STARTING_LOCATION);
        editText.setHint(startingLoc.getString(CURRENT_STARTING_LOCATION, "current location"));

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(view);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String startingAddress = editText.getText().toString();
                Geocoder geocoder = new Geocoder(MainActivity.this);
                List<Address> addresses;

                try {
                    addresses = geocoder.getFromLocationName(startingAddress, 5);
                    if (addresses == null || addresses.size() < 1) {
                        Log.d("starting loc: ", "not found");
                        return;
                    }

                    Address location = addresses.get(0);
                    Double lat = location.getLatitude();
                    Double lon = location.getLongitude();

                    if (startingAddress.equals("current location")) {
                        startingLoc.edit().clear().commit();
                    } else {
                        Log.d("starting loc: ", String.valueOf(lat) + " " + String.valueOf(lon));
                        startingLoc.edit().putString(CURRENT_STARTING_LOCATION, startingAddress).commit();
                        startingLoc.edit().putString(CURRENT_STARTING_LAT, String.valueOf(lat)).commit();
                        startingLoc.edit().putString(CURRENT_STARTING_LON, String.valueOf(lon)).commit();
                    }

                    Toast.makeText(MainActivity.this, "Your starting location has been set to " + startingAddress, Toast.LENGTH_LONG).show();

                } catch (IOException e) {

                }

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
    
}
