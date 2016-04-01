package com.example.jayzhang.LetsGo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jayzhang on 3/25/16.
 */
public class PlaceAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Business> businesses;

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

    public void setBusiness(ArrayList<Business> b) {
        this.businesses = b;
    }

    public ArrayList<Business> getBusiness() {
        return this.businesses;
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

            // **Bug: setting layout params dynamically depending on device screen size
            cardView.setLayoutParams(new GridView.LayoutParams(300, 450));
            cardView.setContentPadding(10, 10, 10, 10);

            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setWeightSum(1);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));
            textView.setTextSize(10);

            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            cardView.addView(linearLayout);
        } else {
            cardView = (CardView) convertView;
        }

        Log.d("position: ", String.valueOf(position));
        Log.d("businesses size: ", String.valueOf(businesses.size()));

        ImageView curImageView = (ImageView) ((LinearLayout)cardView.getChildAt(0)).getChildAt(0);
        TextView curTextView = (TextView) ((LinearLayout)cardView.getChildAt(0)).getChildAt(1);
        // curImageView.setImageResource(R.drawable.sample_0);
        new DownloadImageTask(curImageView).execute(businesses.get(position).imageUrl());
        curTextView.setText(businesses.get(position).name());

        return cardView;

    }
    
    // source: http://stackoverflow.com/questions/29001163/convert-image-url-to-drawable-resource-id-in-android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImageView;
        public DownloadImageTask(ImageView imageView){
            this.mImageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mImageView.setBackground(new BitmapDrawable(mContext.getResources(), result));
        }
    }
}
