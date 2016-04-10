package com.example.jayzhang.LetsGo;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public final static String CURRENT_BUSINESS = "com.example.jayzhang.LetsGo.BUSINESS";
    public final static String CURRENT_BUSINESS_IMAGE = "com.example.jayzhang.LetsGo.BUSINESS_IMAGE";
    public final static String CURRENT_BUSINESS_RATING = "com.example.jayzhang.LetsGo.BUSINESS_RATING";

    private final static String consumerKey = "2q_fORhYgW2bMulCBkVOsw";
    private final static String consumerSecret = "5Xf4mXoItuhF66E373fXLFie1zI";
    private final static String token = "veqUNoadchDOaDdbHd_gUhfJXJX1r9GO";
    private final static String tokenSecret = "ZUZ88amNmp25m_-5oyqY6iTfyzU";

    private SearchView searchView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rn = new Random();
                if (rn.nextInt(1 - 0 + 1) + 0 == 1) {
                    Toast.makeText(MainActivity.this, "Thumbs Up", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(MainActivity.this, "Thumbs Down", Toast.LENGTH_LONG).show();
            }
        });

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
        yelpSearch("College Park, MD");


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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void yelpSearch(String location) {
        TextView textView = (TextView) findViewById(R.id.placeName);
        textView.setText(location);
        progressDialog.show();

        YelpAPIFactory apiFactory = new YelpAPIFactory(consumerKey, consumerSecret, token, tokenSecret);
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        params.put("term", "parks");
        // params.put("category-filter", "hiking");
        // params.put("limit", "20");
        params.put("sort", "2");
        params.put("lang", "fr");

        retrofit.Call<SearchResponse> call = yelpAPI.search(location, params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Response<SearchResponse> response, Retrofit retrofit) {
                SearchResponse searchResponse = response.body();

                int totalNumberOfResult = searchResponse.total();
                ArrayList<Business> businesses = searchResponse.businesses();

                Log.d("total results: ", String.valueOf(totalNumberOfResult));

                createGridView(businesses);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("error: ", t.toString());
            }
        };

        call.enqueue(callback);

    }

    private void createGridView(ArrayList<Business> businesses) {
        final PlaceAdapter placeAdapter = new PlaceAdapter(this);
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

        placeAdapter.setBusiness(businesses);
        placeAdapter.setPlaceImages(imageDrawables);
        placeAdapter.setPlaceRatings(ratingDrawables);

        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(placeAdapter);
        progressDialog.hide();
        Log.d("images: ", "all created");
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlaceActivity.class);
                intent.putExtra(CURRENT_BUSINESS, placeAdapter.getBusiness(position));
                intent.putExtra(CURRENT_BUSINESS_IMAGE, placeAdapter.getPlaceImage(position));
                intent.putExtra(CURRENT_BUSINESS_RATING, placeAdapter.getPlaceRating(position));
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
        searchView.clearFocus();
        yelpSearch(query);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

}
