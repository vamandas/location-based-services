package com.iskconbaroda.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amitshekhar.DebugDB;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iskconbaroda.Constants;
import com.iskconbaroda.R;
import com.iskconbaroda.fragments.PlacesListFragment;
import com.iskconbaroda.maputils.MapAreaManager;
import com.iskconbaroda.maputils.MapAreaMeasure;
import com.iskconbaroda.maputils.MapAreaWrapper;
import com.iskconbaroda.services.FetchAddressIntentService;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static final int REQUEST_CODE_SEARCH_ADDRESS = 101;
    private static final int REQUEST_CODE_CHECK_LOCATION_SETTINGS = 201;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private Marker mMarker;
    private Location mCurrentLocation;
    private LatLng mSelectedLatLng;
    private boolean mRequestingLocationUpdates;
    private AddressResultReceiver mResultReceiver;
    private MapAreaManager mCircleManager;
    private MapAreaWrapper mCircle;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("address1", DebugDB.getAddressLog());
        mResultReceiver = new AddressResultReceiver(new Handler());

        buildGoogleApiClient();

        ((SeekBar) findViewById(R.id.seekBarRadius)).setMax(Constants.MAX_RADIUS_METERS);
        ((SeekBar) findViewById(R.id.seekBarRadius)).setProgress(Constants.DEFAULT_RADIUS_METERS);
        ((SeekBar) findViewById(R.id.seekBarRadius)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int radiusMeters, boolean fromUser) {
                if (mCircle != null) {
                    updateRadius();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_search) {

            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(this);
                startActivityForResult(intent, REQUEST_CODE_SEARCH_ADDRESS);
                overridePendingTransition(0, 0);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }

            return true;

        }

        if (item.getItemId() == R.id.action_add) {

            Location location = new Location("");
            location.setLatitude(mSelectedLatLng.latitude);
            location.setLongitude(mSelectedLatLng.longitude);
            launchAddPlace(location, ((SeekBar) findViewById(R.id.seekBarRadius)).getProgress(), mMarker.getSnippet());

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            }

        }

        if (id == R.id.nav_my_places) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") == null) {
                findViewById(R.id.rlMap).setVisibility(View.GONE);
                findViewById(R.id.rlPlaces).setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().add(R.id.rlPlaces, PlacesListFragment.getInstance(), "PlacesListFragment").commit();
            }

        }

        if (id == R.id.satellite) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            } else
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        }

        if (id == R.id.terrain) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            } else
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        }

        if (id == R.id.hybrid) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            } else
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }

        if (id == R.id.normal) {

            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            } else
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        }

        if (id == R.id.reportabug) {
            Intent i = new Intent(MapsActivity.this, ReportBugActivity.class);
            i.putExtra("TYPE", "bug");
            startActivity(i);
        }

        if (id == R.id.feedback) {
            Intent i = new Intent(MapsActivity.this, ReportBugActivity.class);
            i.putExtra("TYPE", "feedback");
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        stopRequestingLocationUpdates();
        super.onPause();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void checkLocationServiceEnabled() {

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        startRequestingLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CODE_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private void startRequestingLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mRequestingLocationUpdates = true;
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        if (mCurrentLocation != null) {
            mSelectedLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            startIntentService(mCurrentLocation);
        }

    }

    private void stopRequestingLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void updateLocationOnMap(LatLng latLng, String title, String snippet) {

        mMap.clear();
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

        Location mLocation = new Location("Geofence");
        mLocation.setLatitude(latLng.latitude);
        mLocation.setLongitude(latLng.longitude);
        setCircleToLocation(mLocation);

    }

    public void onMapSearch(String locationName) {

        List<Address> addressList = null;

        if (locationName != null || !locationName.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(locationName, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            mMap.animateCamera(zoom);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a mMarker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mSelectedLatLng = latLng;
                Location mLocation = new Location("");
                mLocation.setLatitude(latLng.latitude);
                mLocation.setLongitude(latLng.longitude);
                startIntentService(mLocation);
            }
        });

        setupCircleManager();
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_SEARCH_ADDRESS:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    Log.d("LBS", "hi");
                    Log.i("LBS", "Place: " + place.getAddress() + place.getPhoneNumber() + place.getLatLng().latitude);
                    updateLocationOnMap(place.getLatLng(), place.getName().toString(), place.getAddress().toString());

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e("LBS", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;

            case REQUEST_CODE_CHECK_LOCATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    startRequestingLocationUpdates();
                }
                break;
            default:
                break;
        }

        if (requestCode == 1) {

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationServiceEnabled();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }


    // Radius Circle
    private void setupCircleManager() {
        mCircleManager = new MapAreaManager(mMap,

                4, Color.RED, Color.HSVToColor(70, new float[]{1, 1, 200}), //styling

                -1,//custom drawables for move and resize icons

                0.5f, 0.5f, //sets anchor point of move / resize drawable in the middle

                new MapAreaMeasure(100, MapAreaMeasure.Unit.pixels), //circles will start with 100 pixels (independent of zoom level)

                new MapAreaManager.CircleManagerListener() { //listener for all circle events

                    @Override
                    public void onCreateCircle(MapAreaWrapper draggableCircle) {

                    }

                    @Override
                    public void onMoveCircleEnd(MapAreaWrapper draggableCircle) {

                    }

                    @Override
                    public void onMoveCircleStart(MapAreaWrapper draggableCircle) {

                    }

                });
    }

    // Setup Circle
    private void setCircleToLocation(Location location) {
        // Set Circle
        if (mMap != null && mCircleManager != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mCircle = new MapAreaWrapper(mMap, position, Constants.DEFAULT_RADIUS_METERS, 5.0f, 0xffff0000, 0x33ff0000, 1, 1000);
            mCircleManager.add(mCircle);
            updateRadius();
        }
    }

    private void updateRadius() {
        int radiusMeters = ((SeekBar) findViewById(R.id.seekBarRadius)).getProgress();
        ((TextView) findViewById(R.id.tvRadius)).setText(String.format(getResources().getConfiguration().locale, "%d m", radiusMeters));
        if (mCircle != null) {
            mCircle.setRadius(radiusMeters);
        }
    }


    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().findFragmentByTag("PlacesListFragment") != null) {
                navigationView.getMenu().findItem(R.id.nav_map).setChecked(true);
                findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                findViewById(R.id.rlPlaces).setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("PlacesListFragment")).commit();
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            Log.v("LBS", resultCode + ":" + resultData.getString(Constants.RESULT_DATA_KEY));

            if (resultCode == Constants.SUCCESS_RESULT) {

                updateLocationOnMap(mSelectedLatLng, "Selected Location", resultData.getString(Constants.RESULT_DATA_KEY));

            }
        }
    }
}
