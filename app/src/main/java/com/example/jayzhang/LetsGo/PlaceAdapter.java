package com.example.jayzhang.LetsGo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.yelp.clientlib.entities.Business;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jayzhang on 3/25/16.
 */
public class PlaceAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Business> businesses;
    private ArrayList<Bitmap> placeImages;
    private ArrayList<Bitmap> placeRatings;
    // private static final long serialVersionUID = -7060210544600464481L;

    public PlaceAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return businesses.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public void setBusinesses(ArrayList<Business> b) {
        this.businesses = b;
    }

    public void setPlaceImages(ArrayList<Bitmap> placeImages) {this.placeImages = placeImages; }

    public void setPlaceRatings(ArrayList<Bitmap> placeRatings) {this.placeRatings = placeRatings; }

    public Business getBusiness(int position) {
        return businesses.get(position);
    }

    public Bitmap getPlaceImage(int position) {
        return placeImages.get(position);
    }

    public Bitmap getPlaceRating(int position) {
        return placeRatings.get(position);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        CardView cardView;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            cardView = new CardView(mContext);
            LinearLayout linearLayout = new LinearLayout(mContext);
            ImageView imageView = new ImageView(mContext);
            TextView textView = new TextView(mContext);
            ImageView ratingView = new ImageView(mContext);

            // **Bug: setting layout params dynamically depending on device screen size
            cardView.setLayoutParams(new GridView.LayoutParams(300, 450));
            cardView.setContentPadding(10, 10, 10, 10);

            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setWeightSum(1);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, .6f));
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, .2f));
            ratingView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, .2f));
            textView.setTextSize(10);

            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            linearLayout.addView(ratingView);
            cardView.addView(linearLayout);

        } else {
            cardView = (CardView) convertView;
        }

        ImageView curImageView = (ImageView) ((LinearLayout)cardView.getChildAt(0)).getChildAt(0);
        TextView curTextView = (TextView) ((LinearLayout)cardView.getChildAt(0)).getChildAt(1);
        ImageView curRatingView = (ImageView) ((LinearLayout)cardView.getChildAt(0)).getChildAt(2);

        curImageView.setImageBitmap(placeImages.get(position));
        curTextView.setText(businesses.get(position).name());
        curRatingView.setImageBitmap(placeRatings.get(position));

        return cardView;

    }

    // source: http://stackoverflow.com/questions/3375166/android-drawable-images-from-url
    public static Bitmap bitmapFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return x;
    }

}
