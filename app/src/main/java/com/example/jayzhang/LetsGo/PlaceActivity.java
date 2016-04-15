package com.example.jayzhang.LetsGo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.yelp.clientlib.entities.Business;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;

public class PlaceActivity extends AppCompatActivity {

    String curBusinessName;
    ArrayList<String> curBusinessAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.selectedBusinesses = getSharedPreferences(MainActivity.PREFS_NAME_BUSINESS, MainActivity.PREFS_MODE_BUSINESS).edit();
                MainActivity.selectedBusinesses.putStringSet(curBusinessName, new HashSet<String>(curBusinessAddress));
                MainActivity.selectedBusinesses.commit();
                Toast.makeText(PlaceActivity.this, curBusinessName + " has been saved to your route!", Toast.LENGTH_LONG).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        curBusinessName = intent.getStringExtra(MainActivity.CURRENT_BUSINESS_NAME);
        curBusinessAddress = intent.getStringArrayListExtra(MainActivity.CURRENT_BUSINESS_ADDRESS);
        Bitmap curBusinessImage = intent.getParcelableExtra(MainActivity.CURRENT_BUSINESS_IMAGE);
        Double curBusinessDistance = intent.getDoubleExtra(MainActivity.CURRENT_BUSINESS_DISTANCE, 0);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(curBusinessImage);

        TextView address = (TextView) findViewById(R.id.address);
        address.setText(curBusinessAddress.get(0) + ", " + curBusinessAddress.get(1) + ", " + curBusinessAddress.get(2));
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
            case R.id.action_select:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
