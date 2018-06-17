package com.iskconbaroda.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;

import com.iskconbaroda.Constants;
import com.iskconbaroda.db.MyPlace;

public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog = null;

    protected void showProgressDialog(String progressMessage, boolean isCancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(BaseActivity.this);
        }
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(isCancelable);
        mProgressDialog.setMessage(progressMessage);
        mProgressDialog.show();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    protected void launchAddPlace(Location mCurrentLocation, int radius, String address) {
        Intent addPlaceIntent = new Intent(BaseActivity.this, AddNewActivity.class);
        addPlaceIntent.putExtra(Constants.EXTRA_LAT_LNG, mCurrentLocation);
        addPlaceIntent.putExtra(Constants.EXTRA_RADIUS, radius);
        addPlaceIntent.putExtra(Constants.EXTRA_ADDRESS, address);
        startActivity(addPlaceIntent);
    }

    public void launchAddPlace(MyPlace placeToUpdate) {
        Intent addPlaceIntent = new Intent(BaseActivity.this, AddNewActivity.class);
        addPlaceIntent.putExtra(Constants.EXTRA_VIEW_TYPE, Constants.TYPE_UPDATE);
        addPlaceIntent.putExtra(Constants.EXTRA_PLACE_ID, placeToUpdate.getDbId());
        startActivity(addPlaceIntent);
    }

}
