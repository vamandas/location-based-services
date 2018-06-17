package com.iskconbaroda.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iskconbaroda.R;
import com.iskconbaroda.db.MyPlace;

import java.util.List;

public class PlacesListAdapter extends BaseAdapter{

    private Context mContext = null;
    private List<MyPlace> mPlacesList;
    private OnPlacesActionListener mOnPlacesActionListener = null;

    public PlacesListAdapter(Context mContext, List<MyPlace> mPlacesList, OnPlacesActionListener mOnPlacesActionListener) {
        super();
        this.mContext = mContext;
        this.mPlacesList = mPlacesList;
        this.mOnPlacesActionListener = mOnPlacesActionListener;
    }

    @Override
    public int getCount() {
        return mPlacesList == null ? 0 : mPlacesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlacesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mPlacesList.get(position).getDbId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = checkAndGetView(convertView);
        updateView(position, convertView);
        return convertView;
    }

    private void updateView(int position, View convertView) {
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        MyPlace place = (MyPlace) getItem(position);

        viewHolder.llMain.setTag(place);
        viewHolder.txtTitle.setText(place.getTitle());
        viewHolder.txtMessage.setText(place.getMessage());
        viewHolder.txtReminder.setText(place.getReminder());
        viewHolder.txtAddress.setText(place.getAddress());
        viewHolder.imgShare.setTag(place);
        viewHolder.imgDelete.setTag(place);

    }

    private View checkAndGetView(View convertView) {
        if (convertView == null) {
            convertView = getNewPlaceView();
        }
        return convertView;
    }

    private View getNewPlaceView() {
        View rowView = View.inflate(mContext, R.layout.item_places_list, null);
        setViewHolder(rowView);
        return rowView;
    }

    private void setViewHolder(View rowView) {
        ViewHolder rowViewHolder = new ViewHolder();
        rowViewHolder.llMain = (LinearLayout) rowView.findViewById(R.id.llMain);
        rowViewHolder.llMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPlacesActionListener.onRowItemClick((MyPlace) v.getTag());
            }
        });
        rowViewHolder.txtTitle = (TextView) rowView.findViewById(R.id.txtTitle);
        rowViewHolder.txtMessage = (TextView) rowView.findViewById(R.id.txtMessage);
        rowViewHolder.txtReminder = (TextView) rowView.findViewById(R.id.txtReminder);
        rowViewHolder.txtAddress = (TextView) rowView.findViewById(R.id.txtAddress);
        rowViewHolder.imgShare = (ImageView) rowView.findViewById(R.id.imgShare);
        rowViewHolder.imgShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPlacesActionListener.onClickShare((MyPlace) v.getTag());
            }
        });
        rowViewHolder.imgDelete = (ImageView) rowView.findViewById(R.id.imgDelete);
        rowViewHolder.imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPlacesActionListener.onRowItemLongClick((MyPlace) v.getTag());
            }
        });
        rowView.setTag(rowViewHolder);
    }

    public interface OnPlacesActionListener {
        void onClickShare(MyPlace place);
        void onRowItemClick(MyPlace place);
        void onRowItemLongClick(MyPlace place);
    }

    static class ViewHolder {

        private LinearLayout llMain;

        private TextView txtTitle;

        private TextView txtReminder;

        private TextView txtMessage;

        private TextView txtAddress;

        private ImageView imgShare;

        private ImageView imgDelete;
    }

}
