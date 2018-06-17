package com.iskconbaroda.activities;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.iskconbaroda.Constants;
import com.iskconbaroda.GeofenceBroadcastReceiver;
import com.iskconbaroda.MyApplication;
import com.iskconbaroda.R;
import com.iskconbaroda.db.MyPlace;
import com.iskconbaroda.services.GeofenceTransitionsJobIntentService;

import java.util.List;

import static com.iskconbaroda.Constants.EXTRA_ADDRESS;
import static com.iskconbaroda.Constants.EXTRA_LAT_LNG;
import static com.iskconbaroda.Constants.EXTRA_PLACE_ID;
import static com.iskconbaroda.Constants.EXTRA_RADIUS;
import static com.iskconbaroda.Constants.TYPE_BLUETOOTH_OFF;
import static com.iskconbaroda.Constants.TYPE_BLUETOOTH_ON;
import static com.iskconbaroda.Constants.TYPE_CUSTOM_VOLUME;
import static com.iskconbaroda.Constants.TYPE_MESSAGE;
import static com.iskconbaroda.Constants.TYPE_NORMAL;
import static com.iskconbaroda.Constants.TYPE_SILENT;
import static com.iskconbaroda.Constants.TYPE_SIMPLE_NOTIFICATION;
import static com.iskconbaroda.Constants.TYPE_VIBRATE;
import static com.iskconbaroda.Constants.TYPE_WIFI_OFF;
import static com.iskconbaroda.Constants.TYPE_WIFI_ON;

public class AddNewActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    public static final String TAG = "AddNewActivity";
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private static final int RESULT_PICK_CONTACT = 58500;
    Spinner spActionCategory,spSettingsCategory;
    RadioButton rbEntry,rbExit;
    LinearLayout llSettingsCategory,llCustomVolume,llScheduleaMessage,llReminder;
    private Location mCurrentLocation = null;
    private EditText mEdtTitle = null;
    private EditText mEdtMessage = null;
    private EditText mEdtReminder = null;
    private EditText mEdtContactNo = null;
    private Button mBtnAddPlacce = null;
    private ImageButton mIbContactPicker;
    private int mRadius = Constants.DEFAULT_RADIUS_METERS;
    private String mAddress = "";
    private long mPlaceId = -1;
    private MyPlace mPlaceToUpdate = null; // init only when viewType == TYPE_UPDATE
    private List<MyPlace> mListGeofences = null;
    private PendingIntent mGeofencePendingIntent = null;
    private GeofencingClient mGeofencingClient;
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


    private ResultCallback<Status> mGeofenceResultCallback = new ResultCallback<Status>() {

        @Override
        public void onResult(Status arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mCurrentLocation = getIntent().getParcelableExtra(EXTRA_LAT_LNG);
        mRadius = getIntent().getIntExtra(EXTRA_RADIUS, Constants.DEFAULT_RADIUS_METERS);
        if (getIntent().hasExtra(EXTRA_ADDRESS)) {
            mAddress = getIntent().getStringExtra(EXTRA_ADDRESS);
        }

        mEdtTitle = (EditText) findViewById(R.id.edtTitle);
        mEdtMessage = (EditText) findViewById(R.id.etSMS);
        mEdtReminder = (EditText) findViewById(R.id.etReminder);
        mEdtContactNo = (EditText) findViewById(R.id.etContacts);
        rbEntry = (RadioButton) findViewById(R.id.rbEntry);
        rbExit = (RadioButton) findViewById(R.id.rbExit);

        mIbContactPicker = (ImageButton) findViewById(R.id.ibContactPicker);
        mIbContactPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, RESULT_PICK_CONTACT);
            }
        });

        mBtnAddPlacce = (Button) findViewById(R.id.btnDone);
//        mEdtMessage.setOnEditorActionListener(mOnEditorActionListener);
        spActionCategory = (Spinner) findViewById(R.id.spActionCategory);
        spSettingsCategory = (Spinner) findViewById(R.id.spSettingsCategory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
/*
        mEdtRadius.setText(mRadius + "");
        mTxtAddress.setText(mAddress);
        */
        spActionCategory=(Spinner)findViewById(R.id.spActionCategory);
        llCustomVolume=(LinearLayout)findViewById(R.id.llCustomVolume);
        llSettingsCategory=(LinearLayout)findViewById(R.id.llSettingsCategory);
        llScheduleaMessage=(LinearLayout)findViewById(R.id.llScheduleaMessage);
        llReminder=(LinearLayout)findViewById(R.id.llReminder);
        mBtnAddPlacce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddPlace();
            }
        });
        spActionCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:
                        llSettingsCategory.setVisibility(View.VISIBLE);
                        llScheduleaMessage.setVisibility(View.GONE);
                        llReminder.setVisibility(View.GONE);
                        llCustomVolume.setVisibility(View.GONE);
                        break;

                    case 1:
                        llSettingsCategory.setVisibility(View.GONE);
                        llScheduleaMessage.setVisibility(View.VISIBLE);
                        llReminder.setVisibility(View.GONE);
                        llCustomVolume.setVisibility(View.GONE);
                        break;

                    case 2:
                        llSettingsCategory.setVisibility(View.GONE);
                        llScheduleaMessage.setVisibility(View.GONE);
                        llReminder.setVisibility(View.VISIBLE);
                        llCustomVolume.setVisibility(View.GONE);
                        break;

                    case 3:
                        llSettingsCategory.setVisibility(View.GONE);
                        llScheduleaMessage.setVisibility(View.GONE);
                        llReminder.setVisibility(View.GONE);
                        llCustomVolume.setVisibility(View.VISIBLE);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spActionCategory.setSelection(0);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

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

    @SuppressLint("LongLogTag")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {
                        String phoneNo = null;
                        String name = null;

                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);

                        mEdtContactNo.setText(phoneNo);

                        Log.e("Name & Contact number is",name+","+phoneNo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("Failed", "Not able to pick contact");
        }
    }


    private String getAddress() {
        return mAddress;
    }

    private boolean isTitleValid() {
        String title = mEdtTitle.getText().toString();
        return isValidString(title);
    }

    private boolean isValidString(String title) {
        return title != null && !title.trim().isEmpty();
    }

    private boolean isValidForFencing() {
        mListGeofences = MyApplication.getDatabase(getBaseContext()).getGeoFencedPlaces();
        return mListGeofences.size() < MyApplication.MAX_GEOFENCES || mListGeofences.contains(mPlaceToUpdate);
    }

    private void onClickAddPlace() {

        if (!isValidForFencing()) {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.geofencing_limit_exceeded));
            return;
        }

        if (isTitleValid()) {
            addOrUpdatePlaceData();
        }else {
            mEdtTitle.setError(getString(R.string.error_enter_data));

        }

    }

    private void addOrUpdatePlaceData() {
          addNewPlaceAndFinish();
    }

    private void addNewPlaceAndFinish() {

        MyPlace newMyPlace = getUpdatedPlace();
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


    private void updatePlaceAndFinish(MyPlace placeToUpdate) {
        if (MyApplication.getDatabase(getBaseContext()).updateMyPlace(placeToUpdate.getDbId(), placeToUpdate)) {
            checkAndAddPlaceToGeoFence(placeToUpdate);
        } else {
            MyApplication.showGenericToast(getBaseContext(), getString(R.string.failure));
        }
        finish();
    }

    private MyPlace getUpdatedPlace() {
        MyPlace myPlace = new MyPlace();
        //todo whagt lat lng are being stored..
        myPlace.setLatitude(mCurrentLocation.getLatitude());
        myPlace.setLongitude(mCurrentLocation.getLongitude());
        myPlace.setAddress(getAddress());
        myPlace.setTitle(mEdtTitle.getText().toString().trim());
        myPlace.setCreatedTime(System.currentTimeMillis());
        myPlace.setContactNo(mEdtContactNo.getText().toString());
        myPlace.setMessage(mEdtMessage.getText().toString().trim());
        myPlace.setReminder(mEdtReminder.getText().toString().trim());
        if(rbEntry.isChecked()){
            myPlace.setFenceStatus(0);
        }else
        {
            myPlace.setFenceStatus(1);
        }

        switch (spActionCategory.getSelectedItemPosition())
        {
            case 0:
                switch (spSettingsCategory.getSelectedItemPosition())
                {
                    case 0:
                        myPlace.setActionType(TYPE_SILENT);
                        break;

                    case 1:
                        myPlace.setActionType(TYPE_VIBRATE);
                        break;

                    case 2:
                        myPlace.setActionType(TYPE_NORMAL);
                        break;

                    case 3:
                        myPlace.setActionType(TYPE_WIFI_ON);
                        break;

                    case 4:
                        myPlace.setActionType(TYPE_WIFI_OFF);
                        break;

                    case 5:
                        myPlace.setActionType(TYPE_BLUETOOTH_ON);
                        break;

                    case 6:
                        myPlace.setActionType(TYPE_BLUETOOTH_OFF);
                        break;

                }
                break;

            case 1: myPlace.setActionType(TYPE_MESSAGE);
                break;

            case 2: myPlace.setActionType(TYPE_SIMPLE_NOTIFICATION);
                break;

            case 3: myPlace.setActionType(TYPE_CUSTOM_VOLUME);
                break;

        }

        myPlace.setIsAddedToFence(mRadius > 0);
        if (!myPlace.isAddedToFence()) {
            myPlace.setRadius(MyPlace.RADIUS_DEFAULT);
        }else {
            myPlace.setRadius(mRadius);
        }
        return myPlace;
    }

    private void checkAndAddPlaceToGeoFence(MyPlace newMyPlace) {
            addOrRemoveGeofence(newMyPlace);
    }
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            Toast.makeText(getApplicationContext(),"Geofence successfully added",Toast.LENGTH_SHORT).show();
        } else {

            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);// Get the status code for the error and log it using a user-friendly message.
            Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();

        }
    }

    private void addOrRemoveGeofence(MyPlace place) {
        if (place.isAddedToFence()) {
            mGeofencingClient.addGeofences(getGeofencingRequest(place), getGeoFencePendingIntent(place))
                    .addOnCompleteListener(this);
        } else {
          //  LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeoFencePendingIntent(place));
            mGeofencingClient.removeGeofences(getGeoFencePendingIntent(place)).addOnCompleteListener(this);        }

    }

    private GeofencingRequest getGeofencingRequest(MyPlace place) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
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
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.putExtra(EXTRA_PLACE_ID, place.getDbId());
        return PendingIntent.getBroadcast(this, (int) place.getDbId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
