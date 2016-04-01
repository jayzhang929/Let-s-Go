package com.example.jayzhang.hamburger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import com.yelp.clientlib.entities.Business;
import java.io.InputStream;
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
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(mContext);
            textView.setLayoutParams(new GridView.LayoutParams(400, 400));
            textView.setPadding(4, 4, 4, 4);
        } else {
            textView = (TextView) convertView;
        }

        Log.d("position: ", String.valueOf(position));
        Log.d("businesses size: ", String.valueOf(businesses.size()));
        textView.setText(businesses.get(position).name());
        new DownloadImageTask(textView).execute(businesses.get(position).imageUrl());

        return textView;

    }

    // source: http://stackoverflow.com/questions/29001163/convert-image-url-to-drawable-resource-id-in-android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        TextView mTextView;
        public DownloadImageTask(TextView textView){
            this.mTextView = textView;
        }  protected Bitmap doInBackground(String... urls) {
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
            mTextView.setBackground(new BitmapDrawable(mContext.getResources(), result));
        }
    }
}
