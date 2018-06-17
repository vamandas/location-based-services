package com.iskconbaroda.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.iskconbaroda.Constants;
import com.iskconbaroda.MyApplication;
import com.iskconbaroda.R;
import com.iskconbaroda.db.MyPlace;
import com.iskconbaroda.services.GeofenceTransitionsJobIntentService;
import com.iskconbaroda.utils.GoogleStaticMapsAPIServices;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.iskconbaroda.Constants.EXTRA_ADDRESS;
import static com.iskconbaroda.Constants.EXTRA_LAT_LNG;
import static com.iskconbaroda.Constants.EXTRA_PLACE_ID;
import static com.iskconbaroda.Constants.EXTRA_RADIUS;
import static com.iskconbaroda.Constants.EXTRA_VIEW_TYPE;
import static com.iskconbaroda.Constants.TYPE_ADD;
import static com.iskconbaroda.Constants.TYPE_UPDATE;

public class AddPlaceActivity extends BaseActivity {

    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private Location mCurrentLocation = null;
    private EditText mEdtTitle = null;
    private EditText mEdtMessage = null;
    private TextView mTxtAddress = null;
    private Button mBtnAddPlacce = null;
    private EditText mEdtRadius = null;
    private ImageView ivMapContainer = null;
    private int mCurrentViewType = TYPE_ADD;
    private long mPlaceId = -1;
    private int mRadius = Constants.DEFAULT_RADIUS_METERS;
    private String mAddress = "";
    private MyPlace mPlaceToUpdate = null; // init only when viewType == TYPE_UPDATE
    private List<MyPlace> mListGeofences = null;
    private PendingIntent mGeofencePendingIntent = null;
    private GoogleApiClient mGoogleApiClient = null;
    private boolean isGoogleApiConnected = false;
    private OnConnectionFailedListener mConnectionFailedCallback = new OnConnectionFailedListener() {

        @Override
        public void onConnectionFailed(ConnectionResult arg0) {
            onGoogleApiDisabled();
        }
    };
    private ConnectionCallbacks mGoogleApiCallback = new ConnectionCallbacks() {

        @Override
        public void onConnectionSuspended(int arg0) {
            onGoogleApiDisabled();

        }

        @Override
        public void onConnected(Bundle connectionHint) {
            onGoogleApiEnabled(connectionHint);

        }
    };
    private ResultCallback<Status> mGeofenceResultCallback = new ResultCallback<Status>() {

        @Override
        public void onResult(Status arg0) {

        }
    };
    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (v.getId()) {
                case R.id.edtMessage:
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onClickAddPlace();
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    };
    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View clickedView) {
            switch (clickedView.getId()) {
                case R.id.btnDone:
                    onClickAddPlace();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataFromIntent();
        checkDataAndInflate();
    }

    private void initComponents() {
        mListGeofences = MyApplication.getDatabase(getBaseContext()).getGeoFencedPlaces();
        mPlaceToUpdate = (mCurrentViewType == TYPE_UPDATE) ? MyApplication.getDatabase(getBaseContext()).getPlace(mPlaceId) : null;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(mGoogleApiCallback)
                .addOnConnectionFailedListener(mConnectionFailedCallback).addApi(LocationServices.API).build();
    }

    private void onGoogleApiEnabled(Bundle connectionHint) {
        isGoogleApiConnected = true;
    }

    private void onGoogleApiDisabled() {
        isGoogleApiConnected = false;
    }

    private void checkDataAndInflate() {
        if (isValidData()) {
            initComponents();
            setContentView(R.layout.activity_add_place);
            initViews();
            setUpView();
        } else {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.failure));
            finish();
        }

    }

    private void setUpView()  {
        if (mCurrentViewType == TYPE_UPDATE) {
            updateViewWithPlace(mPlaceToUpdate);
            mCurrentLocation = new Location("");
            mCurrentLocation.setLatitude(mPlaceToUpdate.getLatitude());
            mCurrentLocation.setLongitude(mPlaceToUpdate.getLongitude());
        }
        Picasso.with(this).load(GoogleStaticMapsAPIServices.getStaticMapURL(mCurrentLocation, mRadius)).into(ivMapContainer);
    }

    private void updateViewWithPlace(MyPlace myPlace) {
        mEdtMessage.setText(myPlace.getMessage());
        mEdtTitle.setText(myPlace.getTitle());
        mTxtAddress.setText(myPlace.getAddress());
        mEdtRadius.setText("" + myPlace.getRadius());
        mBtnAddPlacce.setText(getString(R.string.update_place));
    }

    private LatLng getCurrentLocation() {
        return (mCurrentViewType == TYPE_ADD) ? new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()) : new LatLng(
                mPlaceToUpdate.getLatitude(), mPlaceToUpdate.getLongitude());
    }

    private boolean isValidData() {
        boolean result = false;
        if (mCurrentViewType == TYPE_ADD) {
            result = mCurrentLocation != null;
        } else {
            result = mPlaceId > 0;
        }
        return result;
    }

    private void initViews() {
        mEdtMessage = (EditText) findViewById(R.id.edtMessage);
        mEdtTitle = (EditText) findViewById(R.id.edtTitle);
        mEdtRadius = (EditText) findViewById(R.id.edtRadius);
        mTxtAddress = (TextView) findViewById(R.id.txtAddress);
        mBtnAddPlacce = (Button) findViewById(R.id.btnDone);
        ivMapContainer = (ImageView) findViewById(R.id.ivMapContainer);
        mBtnAddPlacce.setOnClickListener(mOnClickListener);
        mEdtMessage.setOnEditorActionListener(mOnEditorActionListener);
        initActionBar();
        mEdtRadius.setText(mRadius + "");
        mTxtAddress.setText(mAddress);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDataFromIntent() {
        mCurrentLocation = getIntent().getParcelableExtra(EXTRA_LAT_LNG);
        mCurrentViewType = getIntent().getIntExtra(EXTRA_VIEW_TYPE, TYPE_ADD);
        mPlaceId = getIntent().getLongExtra(EXTRA_PLACE_ID, -1);
        mRadius = getIntent().getIntExtra(EXTRA_RADIUS, Constants.DEFAULT_RADIUS_METERS);
        if (getIntent().hasExtra(EXTRA_ADDRESS)) {
            mAddress = getIntent().getStringExtra(EXTRA_ADDRESS);
        }
        buildGoogleApiClient();

    }

    private boolean isTitleValid() {
        String title = mEdtTitle.getText().toString();
        return isValidString(title);
    }

    private boolean isMessageValid() {
        String title = mEdtMessage.getText().toString();
        return isValidString(title);
    }

    private boolean isValidString(String title) {
        return title != null && !title.trim().isEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickAddPlace() {

        if (!isValidForFencing()) {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.geofencing_limit_exceeded));
            return;
        }

        if (isTitleValid()) {
            if (isMessageValid()) {
                addOrUpdatePlaceData();

            } else {
                mEdtMessage.setError(getString(R.string.error_enter_data));
            }
        } else {
            mEdtTitle.setError(getString(R.string.error_enter_data));

        }

    }

    private void addOrUpdatePlaceData() {
        if (mCurrentViewType == TYPE_UPDATE) {
            MyPlace placeToUpdate = getUpdatedPlace();
            updatePlaceAndFinish(placeToUpdate);
        } else {
            addNewPlaceAndFinish(mCurrentLocation, mEdtTitle.getText().toString().trim(), mEdtMessage.getText().toString().trim());
        }
    }

    private void updatePlaceAndFinish(MyPlace placeToUpdate) {
        if (MyApplication.getDatabase(getBaseContext()).updateMyPlace(placeToUpdate.getDbId(), placeToUpdate)) {
            checkAndAddPlaceToGeoFence(placeToUpdate);
        } else {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.failure));
        }
        finish();
    }

    private MyPlace getUpdatedPlace() {
        mPlaceToUpdate.setAddress(mTxtAddress.getText().toString().trim());
        mPlaceToUpdate.setMessage(mEdtMessage.getText().toString().trim());
        mPlaceToUpdate.setTitle(mEdtTitle.getText().toString().trim());
        mPlaceToUpdate.setIsAddedToFence(true);
        if (!mPlaceToUpdate.isAddedToFence()) {
            mPlaceToUpdate.setRadius(MyPlace.RADIUS_DEFAULT);
        }
        mPlaceToUpdate.setRadius(Integer.parseInt(mEdtRadius.getText().toString().trim()));
        return mPlaceToUpdate;
    }

    private boolean isValidForFencing() {
        return mListGeofences.size() < MyApplication.MAX_GEOFENCES || mListGeofences.contains(mPlaceToUpdate);
    }

    private void addNewPlaceAndFinish(Location curreLocation, String title, String message) {

        MyPlace newMyPlace = getData(curreLocation, title, message);
        long result = MyApplication.getDatabase(getBaseContext()).insertMyPlace(newMyPlace);
        if (result > 0) {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.place_added));
            if (newMyPlace.isAddedToFence()) {
                newMyPlace.setDbId(result);
                checkAndAddPlaceToGeoFence(newMyPlace);
            }
        } else {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.failure));
        }
        finish();
    }

    private void checkAndAddPlaceToGeoFence(MyPlace newMyPlace) {
        if (isGoogleApiConnected) {
            addOrRemoveGeofence(newMyPlace);
        } else {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.failure));
        }
    }

    private void addOrRemoveGeofence(MyPlace place) {
        if (place.isAddedToFence()) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(place), getGeoFencePendingIntent(place))
                    .setResultCallback(mGeofenceResultCallback);
        } else {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeoFencePendingIntent(place));
        }

    }

    private MyPlace getData(Location curreLocation, String title, String message) {
        String address = getAddress();
        return new MyPlace(address, title, message,"", "", 0, 0, curreLocation.getLatitude(), curreLocation.getLongitude(), true,
                System.currentTimeMillis(), Integer.valueOf(mEdtRadius.getText().toString()));
    }

    private String getAddress() {
        return mAddress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private GeofencingRequest getGeofencingRequest(MyPlace place) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(getGeoFenceForPlace(place));
        return builder.build();
    }

    private Geofence getGeoFenceForPlace(MyPlace place) {
        return new Geofence.Builder().setRequestId("" + place.getDbId())
                .setCircularRegion(place.getLatitude(), place.getLongitude(), place.getRadius())
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build();
    }

    private PendingIntent getGeoFencePendingIntent(MyPlace place) {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsJobIntentService.class);
        intent.putExtra(EXTRA_PLACE_ID, place.getDbId());
        return PendingIntent.getService(this, (int) place.getDbId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
