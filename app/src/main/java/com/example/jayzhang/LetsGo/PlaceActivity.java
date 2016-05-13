package com.example.jayzhang.LetsGo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

public class PlaceActivity extends AppCompatActivity {

    String curBusinessName;
    ArrayList<String> curBusinessAddress;
    ArrayList<String> mCurrentBusinessLatLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
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

        // static data for testing purpose
        TextView price = (TextView) findViewById(R.id.price);
        price.setText("$");

        // static data for testing purpose
        TextView distance = (TextView) findViewById(R.id.distance);
        distance.setText("15 miles");

        TextView address = (TextView) findViewById(R.id.address);
        address.setText(curBusinessAddress.get(0) + ", " + curBusinessAddress.get(1) + ", " + curBusinessAddress.get(2));

        final String lat = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_LAT);
        final String lon = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_LON);

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

        MapGenerator mapGenerator = new MapGenerator(PlaceActivity.this,
                                                    ((MapFragment) getFragmentManager().findFragmentById(R.id.business_map)).getMap(),
                                                    null,
                                                    true);

        mapGenerator.drawMarkersMap(Double.parseDouble(lat), Double.parseDouble(lon));
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

}
