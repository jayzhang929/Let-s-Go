package com.example.jayzhang.LetsGo;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by jayzhang on 4/26/16.
 */
public class RetainedFragment extends Fragment {
    private PlaceAdapter mPlaceAdapter;
    private String mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public void setPlaceAdapter (PlaceAdapter placeAdapter) {
        this.mPlaceAdapter = placeAdapter;
    }

    public PlaceAdapter getPlaceAdapter () {
        return this.mPlaceAdapter;
    }

    public void setName (String name) {
        mName = name;
    }

    public String getName () {
        return mName;
    }
}
