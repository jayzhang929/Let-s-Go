package com.example.jayzhang.LetsGo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;


/**
 * Created by jayzhang on 4/20/16.
 */
public class RouteAdapter extends DragItemAdapter<Pair<Long, String>, RouteAdapter.ViewHolder> {

    /*
    private Context mContext;
    private int mLayoutResourceId;
    private String[] mPlaceNames;
    */
    /*
    public RouteAdapter (Context context, int layoutResourceId, String[] placeNames) {
        super(context, layoutResourceId, placeNames);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mPlaceNames = placeNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolderItem;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (LinearLayout) layoutInflater.inflate(mLayoutResourceId, parent, false);
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.mTextView = (TextView) convertView.findViewById(R.id.itemText);
            convertView.setTag(viewHolderItem);
        } else
            viewHolderItem = (ViewHolderItem) convertView.getTag();

        viewHolderItem.mTextView.setText(mPlaceNames[position]);

        return convertView;
    }
    */

    private int mLayoutId;
    private int mGrabHandleId;

    public RouteAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).second;
        holder.mText.setText(text);
        holder.itemView.setTag(text);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter<Pair<Long, String>, RouteAdapter.ViewHolder>.ViewHolder {
        public TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.text);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
