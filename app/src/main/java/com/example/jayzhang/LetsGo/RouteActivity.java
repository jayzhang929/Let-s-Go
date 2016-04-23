package com.example.jayzhang.LetsGo;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jayzhang.LetsGo.R;
import com.squareup.okhttp.Route;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DragListView listView = (DragListView) findViewById(R.id.routeList);
        listView.getRecyclerView().setVerticalScrollBarEnabled(true);
        listView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                Toast.makeText(RouteActivity.this, "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    Toast.makeText(RouteActivity.this, "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ArrayList<Pair<Long, String>> itemArray = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            itemArray.add(new Pair<Long, String>(Long.valueOf(i), "Item " + i));
        }

        listView.setLayoutManager(new LinearLayoutManager(this));
        RouteAdapter routeAdapter = new RouteAdapter(itemArray, R.layout.list_item, R.id.image, false);
        listView.setAdapter(routeAdapter, true);
        listView.setCanDragHorizontally(false);
        listView.setCustomDragItem(new MyDragItem(this, R.layout.list_item));
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView)clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
        }
    }

}
