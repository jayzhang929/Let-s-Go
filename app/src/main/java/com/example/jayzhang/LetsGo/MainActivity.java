package com.example.jayzhang.LetsGo;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.squareup.okhttp.Route;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener, OnMenuItemClickListener {

    public final static String CURRENT_BUSINESS_ADDRESS = "com.example.jayzhang.LetsGo.BUSINESS_ADDRESS";
    public final static String CURRENT_BUSINESS_IMAGE = "com.example.jayzhang.LetsGo.BUSINESS_IMAGE";
    public final static String CURRENT_BUSINESS_NAME = "com.example.jayzhang.LetsGo.BUSINESS_NAME";
    public final static String CURRENT_BUSINESS_RATING = "com.example.jayzhang.LetsGo.BUSINESS_RATING";
    public final static String CURRENT_BUSINESS_DISTANCE = "com.example.jayzhang.LetsGo.BUSINESS_DISTANCE";
    public final static String CURRENT_BUSINESS_LAT = "com.example.jayzhang.LetsGo.BUSINESS_LAT";
    public final static String CURRENT_BUSINESS_LON = "com.example.jayzhang.LetsGo.BUSINESS_LON";
    static final String CURRENT_PLACE = "currentPlace";
    static final String CURRENT_TOPIC = "currentTopic";
    public final String PREFS_NAME = "SharedPrefs";
    public final int PREFS_MODE = 0;
    public static final String PREFS_NAME_BUSINESS = "selectedBusiness";
    public static final int PREFS_MODE_BUSINESS = 1;

    private final static String consumerKey = "2q_fORhYgW2bMulCBkVOsw";
    private final static String consumerSecret = "5Xf4mXoItuhF66E373fXLFie1zI";
    private final static String token = "veqUNoadchDOaDdbHd_gUhfJXJX1r9GO";
    private final static String tokenSecret = "ZUZ88amNmp25m_-5oyqY6iTfyzU";

    private SearchView searchView;
    private ProgressDialog progressDialog;
    private PlaceAdapter mPlaceAdapter;
    private String mCurrentPlace;
    private String mCurrentTopic;
    private SharedPreferences.Editor editor;
    public static SharedPreferences.Editor selectedBusinesses;
    private String mRandomPlace;
    private ArrayList<Business> mRandomRestaurants;
    private ArrayList<Business> mRandomOutdoors;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Wait while Let's Go find your destination :)");

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
        Log.d("selectedBusiness: ", getSharedPreferences(PREFS_NAME_BUSINESS, PREFS_MODE_BUSINESS).getAll().toString());

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, PREFS_MODE);
        mCurrentPlace = sharedPreferences.getString(CURRENT_PLACE, "College Park, MD");
        mCurrentTopic = sharedPreferences.getString(CURRENT_TOPIC, "parks");

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

        // editor.clear();
        // editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = new SearchView(this);
        menu.findItem(R.id.search).setActionView(searchView);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setIconified(true);
        searchView.setFocusable(true);
        searchView.setQueryHint("Let's Go to ...");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.route) {
            Intent intent = new Intent(MainActivity.this, RouteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_clear_route) {
            getSharedPreferences(PREFS_NAME_BUSINESS, PREFS_MODE_BUSINESS).edit().clear().commit();
        } else if (id == R.id.nav_random_generate) {
            BusinessesRandomGenerator businessesRandomGenerator = new BusinessesRandomGenerator();
            String[] params = {mCurrentPlace, null};
            businessesRandomGenerator.execute(params);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private class BusinessesRandomGenerator extends AsyncTask<String, Void, ArrayList<Business>> {

        @Override
        protected ArrayList<Business> doInBackground(String... params) {
            return yelpSearch(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Business> businesses) {
            mRandomPlace = mCurrentPlace;
            mRandomRestaurants = new ArrayList<>();
            mRandomOutdoors = new ArrayList<>();

            for (Business business : businesses) {
                Log.d("category: ", business.categories().toString());
                if (business.categories().contains("coffee") || business.categories().contains("pizza") || business.categories().contains("delis"))
                    mRandomRestaurants.add(business);
                else
                    mRandomOutdoors.add(business);
            }

            // clear current route first
            getSharedPreferences(PREFS_NAME_BUSINESS, PREFS_MODE_BUSINESS).edit().clear().commit();

            if (mRandomOutdoors.size() > 0) {
                int random = randomNumber(mRandomOutdoors.size());
                Log.d("outdoor size: ", String.valueOf(mRandomOutdoors.size()));
                Log.d("random: ", String.valueOf(random));
                Business curRandomOutdoor = mRandomOutdoors.get(random);
                addBusinessToSelectedBusinesses(curRandomOutdoor);
            }

            if (mRandomRestaurants.size() > 0) {
                int random = randomNumber(mRandomRestaurants.size());
                Business curRandomRestaurant = mRandomRestaurants.get(random);
                addBusinessToSelectedBusinesses(curRandomRestaurant);
            }

            if (mRandomOutdoors.size() > 1) {
                int random = randomNumber(mRandomOutdoors.size());
                Business curRandomOutdoor = mRandomOutdoors.get(random);
                addBusinessToSelectedBusinesses(curRandomOutdoor);
            }

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }
    }

    private void addBusinessToSelectedBusinesses (Business business) {
        String name = business.name();
        String lat = String.valueOf(business.location().coordinate().latitude());
        String lon = String.valueOf(business.location().coordinate().longitude());
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>();
        hashSet.add(lat);
        hashSet.add(lon);

        MainActivity.selectedBusinesses = getSharedPreferences(MainActivity.PREFS_NAME_BUSINESS, MainActivity.PREFS_MODE_BUSINESS).edit();
        MainActivity.selectedBusinesses.putStringSet(name, hashSet);
        MainActivity.selectedBusinesses.commit();
    }

    private int randomNumber (int max) {
        Random random = new Random();
        return random.nextInt(max);
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

    private PlaceAdapter createPlaceAdapter(ArrayList<Business> businesses) {
        PlaceAdapter placeAdapter = new PlaceAdapter(this);
        ArrayList<Bitmap> imageDrawables = new ArrayList<Bitmap>();
        ArrayList<Bitmap> ratingDrawables = new ArrayList<Bitmap>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
        progressDialog.hide();
        Log.d("images: ", "all created");
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
        return true;
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

    public void onCategoriesClick(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.category_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuFestival:
                mCurrentTopic = "festival";
                break;
            case R.id.menuHiking:
                mCurrentTopic = "hiking";
                break;
            case R.id.menuMuseums:
                mCurrentTopic = "museums";
                break;
            case R.id.menuParks:
                mCurrentTopic = "parks";
                break;
            case R.id.menuRestaurants:
                mCurrentTopic = "restaurants";
                break;
        }

        yelpAsyncSearch();
        return true;
    }

    private void yelpAsyncSearch() {
        TextView textView = (TextView) findViewById(R.id.placeName);
        textView.setText(mCurrentPlace);
        ((TextView) findViewById(R.id.category_text)).setText(mCurrentTopic);
        progressDialog.show();

        String[] params = {mCurrentPlace, mCurrentTopic};
        YelpSearch yelpSearch = new YelpSearch();
        yelpSearch.execute(params);
    }
}
