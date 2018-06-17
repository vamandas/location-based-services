package com.iskconbaroda.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iskconbaroda.MyApplication;
import com.iskconbaroda.R;
import com.iskconbaroda.activities.BaseActivity;
import com.iskconbaroda.adapter.PlacesListAdapter;
import com.iskconbaroda.db.MyPlace;

import java.util.ArrayList;
import java.util.List;

public class PlacesListFragment extends Fragment {

    public static final String TAG = PlacesListFragment.class.getSimpleName();

    private PlacesListAdapter mAdapterPlaces = null;
    private List<MyPlace> mPlacesList = null;
    private ListView mlstPlaces = null;
    private PlacesListAdapter.OnPlacesActionListener mPlacesActionListener = new PlacesListAdapter.OnPlacesActionListener() {

        @Override
        public void onRowItemClick(MyPlace place) {
            onClickPlace(place);
        }

        @Override
        public void onRowItemLongClick(final MyPlace place) {
            Snackbar snackbar = Snackbar.make(getView(), "Do you want to delete this entry?", Snackbar.LENGTH_LONG);
            snackbar.setAction("YES", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.getDatabase(getContext()).deleteMyPlace(place.getDbId());
                    updatePlaces();
                }
            });
            snackbar.show();
        }

        @Override
        public void onClickShare(MyPlace place) {
            onClickPlaceShare(place);
        }
    };

    /**
     * returns new instance of OrderTracking
     */
    public static PlacesListFragment getInstance() {
        PlacesListFragment placesListFragment = new PlacesListFragment();
        Bundle args = new Bundle();
        placesListFragment.setArguments(args);
        return placesListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_places_list, container, false);
        initFragment(rootView);
        return rootView;
    }

    private void initFragment(View rootView) {
        initDataComponents();
        initViews(rootView);
    }

    private void initDataComponents() {
        mPlacesList = new ArrayList<>();
        mAdapterPlaces = new PlacesListAdapter(getActivity(), mPlacesList, mPlacesActionListener);
    }

    private void initViews(View rootView) {
        mlstPlaces = (ListView) rootView.findViewById(R.id.lstPlaces);
        mlstPlaces.setAdapter(mAdapterPlaces);
        mlstPlaces.setEmptyView(rootView.findViewById(R.id.empty_view));
    }

    private void onClickPlace(MyPlace place) {
        ((BaseActivity) getActivity()).launchAddPlace(place);
    }

    private void onClickPlaceShare(MyPlace place) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(place.getShareUri());
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            MyApplication.showGenericToast(getActivity(), getString(R.string.failure));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlaces();
    }

    private void updatePlaces() {
        mPlacesList.clear();
        mPlacesList.addAll(MyApplication.getDatabase(getActivity()).getMyPlaces());
        mAdapterPlaces.notifyDataSetChanged();
    }

}
