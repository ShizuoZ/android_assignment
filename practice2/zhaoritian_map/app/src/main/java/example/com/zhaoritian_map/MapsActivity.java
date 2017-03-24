package example.com.zhaoritian_map;

import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView latitude;
    private TextView longitude;
    private TextView accuracy;
    private TextView mLastUpdateTimeTextView;
    private TextView mLocationAddressTextView;
    private Button mFetchAddressButton;
    private Button mCheckinButton;
    private Spinner mCheckinData;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private Marker BuschCampusCenter;
    private Marker HighPointSolutionsStadium;
    private Marker ElectricalEngineeringBuilding;
    private Marker RutgersStudentCenter;
    private Marker OldQueen;
    private double dis2bcc;
    private double dis2hss;
    private double dis2eeb;
    private double dis2rsc;
    private double dis2oq;

    private Location mCurrentLocation;
    private Location mLastLocation;

    protected String mAddressOutput;
    protected boolean mAddressRequested;
    protected LatLng locationpin;
    private AddressResultReceiver mResultReceiver;
    private String mLastUpdateTime;
    private checkinHelper db;
    private heatmapHelper dbHeat;
    private final LatLng BSC = new LatLng(40.523128, -74.458797);
    private final LatLng HSS = new LatLng(40.513817, -74.464844);
    private final LatLng EEB = new LatLng(40.521663, -74.460665);
    private final LatLng RSC = new LatLng(40.502661, -74.451771);
    private final LatLng OQ = new LatLng(40.498720, -74.446229);
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    public static final String TAG = "MapsActivity";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mAddressRequested = false;
        mLastUpdateTime = "time initializing...";
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.lasttime);
        mLocationAddressTextView = (TextView) findViewById(R.id.address);
        mFetchAddressButton = (Button) findViewById(R.id.mFetchAddressButton);
        mCheckinButton = (Button) findViewById(R.id.checkinButton);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        accuracy = (TextView) findViewById(R.id.accuracy);
        mCheckinData = (Spinner) findViewById(R.id.spinner);
        mCheckinButton.setOnClickListener(checkinListener);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mMap.setMyLocationEnabled(true);
        db = new checkinHelper(getBaseContext());
        dbHeat = new heatmapHelper(getBaseContext());
        db.open();
        dbHeat.open();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(5 * 1000); // 1 second, in milliseconds
    }

    private OnClickListener checkinListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            saveheat();
            heatmapUpdate();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        locationpin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //mLastLocation=location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        startIntentService();
        updateUI();
        showdistance();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mMap.setMyLocationEnabled(true);
        BuschCampusCenter = mMap.addMarker(new MarkerOptions()
                .position(BSC)
                .title("Busch Campus Center"));
        HighPointSolutionsStadium = mMap.addMarker(new MarkerOptions()
                .position(HSS)
                .title("HighPoint Solutions Stadium"));
        ElectricalEngineeringBuilding = mMap.addMarker(new MarkerOptions()
                .position(EEB)
                .title("Electrical Engineering Building"));
        RutgersStudentCenter = mMap.addMarker(new MarkerOptions()
                .position(RSC)
                .title("Rutgers Student Center"));
        OldQueen = mMap.addMarker(new MarkerOptions()
                .position(OQ)
                .title("Old Queens"));
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
// Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.

        if (mLastLocation != null) {
            latitude.setText(String.valueOf(mLastLocation.getLatitude()));
            longitude.setText(String.valueOf(mLastLocation.getLongitude()));
            accuracy.setText(String.valueOf(mLastLocation.getAccuracy()));
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
        } else {
            startLocationUpdates();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

/*    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        mCurrentLocation = new LatLng(currentLatitude, currentLongitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }*/

    private void updateUI() {
        mLastUpdateTimeTextView.setText("last update time: " + mLastUpdateTime + "  ");
        latitude.setText("latitude:" + String.valueOf(mCurrentLocation.getLatitude()));
        longitude.setText("longitude:" + String.valueOf(mCurrentLocation.getLongitude()));
        accuracy.setText("accuracy:" + String.valueOf(mCurrentLocation.getAccuracy()));
        Log.v(TAG, "latitude" + String.valueOf(mCurrentLocation.getLatitude()));
    }


    public void showdistance() {
        dis2bcc = SphericalUtil.computeDistanceBetween(locationpin, BuschCampusCenter.getPosition());
        BuschCampusCenter.setSnippet("Distance to here:" + formatNumber(dis2bcc));
        dis2hss = SphericalUtil.computeDistanceBetween(locationpin, HighPointSolutionsStadium.getPosition());
        HighPointSolutionsStadium.setSnippet("Distance to here:" + formatNumber(dis2hss));
        dis2eeb = SphericalUtil.computeDistanceBetween(locationpin, ElectricalEngineeringBuilding.getPosition());
        ElectricalEngineeringBuilding.setSnippet("Distance to here:" + formatNumber(dis2eeb));
        dis2rsc = SphericalUtil.computeDistanceBetween(locationpin, RutgersStudentCenter.getPosition());
        RutgersStudentCenter.setSnippet("Distance to here:" + formatNumber(dis2rsc));
        dis2oq = SphericalUtil.computeDistanceBetween(locationpin, OldQueen.getPosition());
        OldQueen.setSnippet("Distance to here:" + formatNumber(dis2oq));
    }

    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    public void fetchAddressButtonHandler(View view) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, process the user's request by
        // setting mAddressRequested to true. Later, when GoogleApiClient connects,
        // launch the service to fetch the address. As far as the user is
        // concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateAddress();
        checkin();
        loadSpinnerData();
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }
            mAddressRequested = false;
            updateAddress();
        }
    }

    private void updateAddress(){
        if(mAddressRequested) {
            mFetchAddressButton.setEnabled(false);
        }else{
            mFetchAddressButton.setEnabled(true);
        }
    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void displayAddressOutput() {
        mLocationAddressTextView.setText(mAddressOutput);
    }

    private void checkin() {
        db.open();
        db.insertLocation(mCurrentLocation, mAddressOutput, mLastUpdateTime);
        Log.v(TAG,"address when check-in" + mAddressOutput);
        writeDB();
        db.close();
    }

    private void saveheat(){
        dbHeat.open();
        dbHeat.insertLocation(mCurrentLocation);
        dbHeat.close();
    }

    private List<String> getData() {
        List<String> locationdata = new ArrayList<String>();
        db.open();
        Cursor cursor = db.query();
        if (cursor.moveToLast()) {
            do {
                locationdata.add(cursor.getString(3) +
                        " " + cursor.getString(2));
                Log.v(TAG,"address" + cursor.getString(3)+""+cursor.getString(2));
            } while (cursor.moveToPrevious());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning lables
        return locationdata;
    }

    private void loadSpinnerData() {
        // Spinner Drop down elements
        List<String> lables = getData();
        Log.v(TAG, "Spinner" + lables.size());
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mCheckinData.setAdapter(dataAdapter);
    }

    private List<LatLng> getHeatData() {
        List<LatLng> List = new ArrayList<LatLng>();
        dbHeat.open();
        Cursor cursor = dbHeat.query();
        if (cursor.moveToFirst()) {
            do {
                String lat = cursor.getString(0);
                String lng = cursor.getString(1);
                LatLng heatLatLng = new LatLng(Double.valueOf(cursor.getString(0)),
                        Double.valueOf(cursor.getString(1)));
                List.add(heatLatLng);
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        dbHeat.close();
        // returning lables
        return List;
    }

    private void heatmapUpdate(){// Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

// Create the tile provider.
        List<LatLng> mHeatLatLng = getHeatData();
        Log.v(TAG,"HEAT"+ mHeatLatLng.size());
        mProvider = new HeatmapTileProvider.Builder()
                .data(mHeatLatLng)
                .gradient(gradient)
                .build();

// Add the tile overlay to the map.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        mProvider.setOpacity(0.7);
        mOverlay.clearTileCache();
    }
    private void writeDB(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//example.com.zhaoritian_map//databases//LocationDatabase";
                String backupDBPath = "LocationDatabase";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }
}
