package com.eew.balloonride.activity;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.eew.balloonride.R;
import com.eew.balloonride.adapter.PlaceArrayAdapter;
import com.eew.balloonride.model.GetLanLagModel;
import com.eew.balloonride.parser.DirectionsJSONParser;
import com.eew.balloonride.utils.ConnectivityUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, LocationListener, View.OnClickListener,PlaceArrayAdapter.CheckInternetStatus {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "EkovBpk2HQccWAFUgLtt7AZIg";
    private static final String TWITTER_SECRET = "LpCxtwaeQ17Ynr2PQVUmGov543gXMKyxh8GldMvOCHdaM8UaBi";

    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteTextView;
    private AutoCompleteTextView mAutocompleteTextView1;
    private EditText mEdtHeight;
    private EditText mEdtSpeed;

    private Button mShowMap;
    private Button mBtnStart;
    private Button mBtnStope;
    private RelativeLayout mRelLayFields;
    private RelativeLayout mRlvtSpeedFields;
    private RelativeLayout mRlvtInternetConection;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    String TAG = "MainActivity";

    // private GoogleMap googleMap;
    private Marker mTempMarker;
    private ArrayList<LatLng> mPermantMarkerPoints = new ArrayList<LatLng>();
    private ArrayList<LatLng> mtempppp = new ArrayList<LatLng>();
    private ArrayList<LatLng> mTempMarkerPoints;
    private boolean flag = false;
    private boolean flagBaloonView = false;
    private ArrayList<LatLng> mBaloonViewTempMarkerPoints = new ArrayList<LatLng>();
    private LatLng mStartPointLangLAt;

    ArrayList<LatLng> mMarkerPoints;
    ArrayList<LatLng> mBaloonViewMarkerPoints;

    android.app.FragmentTransaction ft;
    FragmentManager fragmentManager;

    GoogleMap mGoogleMap;

    double mLatitude = 0;
    double mLongitude = 0;

    private String duration;
    private String distance = null;

    private boolean startMarker = false;
    private double threadSleepPeriod = 0;

    SharedPreferences sharedpreferences;

    private static GetLanLagModel latLanModel;
    ProgressDialog ringProgressDialog = null;

    private Animation slideUp, slideDown;
    private boolean internetStatus=false;

    AutoCompleteTextView atvPlaces;

    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    GoogleMap googleMap;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        setContentView(R.layout.activity_main);



        launchRingDialog(getString(R.string.fetching_your_location));
        // Initializing
        mMarkerPoints = new ArrayList<LatLng>();
        mBaloonViewMarkerPoints = new ArrayList<LatLng>();
        fragmentManager = getFragmentManager();
        ft = fragmentManager.beginTransaction();
        latLanModel = GetLanLagModel.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAutocompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        mAutocompleteTextView.setThreshold(1);

        mAutocompleteTextView1 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        mAutocompleteTextView1.setThreshold(1);

        mEdtHeight = (EditText) findViewById(R.id.edtHeight);
        mEdtSpeed = (EditText) findViewById(R.id.edtSpeed);


        mRelLayFields = (RelativeLayout) findViewById(R.id.relLayFields);
        mRelLayFields.setVisibility(View.VISIBLE);

        mRlvtSpeedFields = (RelativeLayout) findViewById(R.id.rlvtSpeedFields);
        mRlvtSpeedFields.setVisibility(View.GONE);

        mRlvtInternetConection = (RelativeLayout) findViewById(R.id.rlvtInternetConection);
        mRlvtInternetConection.setVisibility(View.GONE);


        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_to_bottom);
        slideUp.setAnimationListener(animationListener);
        slideDown.setAnimationListener(animationListener);

        mShowMap = (Button) findViewById(R.id.showMap);
        mShowMap.setOnClickListener((View.OnClickListener) this);

        mBtnStart = (Button) findViewById(R.id.btnStart);
        mBtnStart.setOnClickListener((View.OnClickListener) this);

        mBtnStope = (Button) findViewById(R.id.btnStope);
        mBtnStope.setOnClickListener((View.OnClickListener) this);

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mAutocompleteTextView1.setOnItemClickListener(mAutocompleteClickListener1);

        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null,this);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);

        mAutocompleteTextView1.setAdapter(mPlaceArrayAdapter);


        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else { // Google Play Services are available

            // Initializing
            mMarkerPoints = new ArrayList<LatLng>();

            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting Map for the SupportMapFragment
            mGoogleMap = fm.getMap();

            // Enable MyLocation Button in the Map
            mGoogleMap.setMyLocationEnabled(false);

            //Set MapType AS (Notmal,Satellite and terrain View)
            // googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            // mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                // onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 20000, 0, (LocationListener) this);


            mShowMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    hideKeyBoard();

                    if (!ConnectivityUtils.isNetworkEnabled(MainActivity.this)) {
                        //Toast.makeText(MainActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                        getInternetStatus(false);
                        return;
                    }else {
                        getInternetStatus(true);
                    }

                    if (validation()) {
                        return;
                    }

                    launchRingDialog(getString(R.string.redirect_to_your_location));
                    // Checks, whether start and end locations are captured
                    if (mMarkerPoints.size() >= 2) {

                        /*Assign LatLan Array list for Baloon view*/
                        mBaloonViewMarkerPoints = mMarkerPoints;

                        LatLng origin = mMarkerPoints.get(0);
                        LatLng dest = mMarkerPoints.get(1);
                        drawMarker();
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                        // zoomLevel = 19 - Math.log(altitudeKm * 5.508);
                    }
                }
            });

        }


        if (!ConnectivityUtils.isNetworkEnabled(this)) {
            //Toast.makeText(MainActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
            getInternetStatus(false);
            cancelRingDialog();
            return;
        }
    }

    /**
     * Validation for EditText fields
     */

    private boolean validation() {
        if (mAutocompleteTextView.getText().toString().trim().equals("")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_source), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView.requestFocus();
            return true;
        } else if (!mAutocompleteTextView.getText().toString().trim().contains(",")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_valid_source), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView.requestFocus();
            return true;
        } else if (mAutocompleteTextView1.getText().toString().trim().equals("")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_dest), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView1.requestFocus();
            return true;
        } else if (!mAutocompleteTextView1.getText().toString().trim().contains(",")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_valid_dest), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView1.requestFocus();
            return true;
        } else if (mEdtHeight.getText().toString().trim().equals("")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_height), Toast.LENGTH_SHORT).show();
            mEdtHeight.requestFocus();
            return true;
        } else if (mMarkerPoints.size() <= 0 && !mAutocompleteTextView.getText().toString().trim().equals("")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_valid_source), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView.requestFocus();
            return true;
        } else if (mMarkerPoints.size() <= 1 && !mAutocompleteTextView1.getText().toString().trim().equals("")) {
            mGoogleMap.clear();
            Toast.makeText(MainActivity.this, getString(R.string.enter_valid_dest), Toast.LENGTH_SHORT).show();
            mAutocompleteTextView1.requestFocus();
            return true;
        }

        return false;
    }

    /*
    * Hide Keyboard after pressign Get View button
    * */
    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAutocompleteTextView.getWindowToken(), 0);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception :", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void getInternetStatus(boolean status) {

        if (status){

            if (internetStatus) {
                mRlvtInternetConection.startAnimation(slideDown);
                //  mRlvtInternetConection.setVisibility(View.GONE);
                internetStatus=false;
            }

        }else {
            if (!internetStatus) {
                // mRlvtInternetConection.setVisibility(View.VISIBLE);
                mRlvtInternetConection.startAnimation(slideUp);
                internetStatus = true;
            }

        }


    }


    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            mBaloonViewTempMarkerPoints.clear();
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");

                        String[] parts = distance.split(" ");
                        distance = parts[0];

                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    mBaloonViewTempMarkerPoints.add(position);
                    points.add(position);

                }

                // Adding all the points in the route to LineOptions

                latLanModel.setArraLatLan(points);

                mTempMarkerPoints = points;
                if (flag == false) {
                    mtempppp = points;
                    flag = true;
                }

                lineOptions.addAll(points);
                lineOptions.width(25);
                lineOptions.geodesic(true);
                lineOptions.color(getResources().getColor(R.color.nav_item_blue));
                // lineOptions.color(getResources().getDrawable(R.drawable.navigation_gradiant));

            }


            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);

            if (mMarkerPoints.size() > 0) {

                LatLng position = mMarkerPoints.get(0);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo((float) getZoomLevel()));
                mRelLayFields.setVisibility(View.GONE);
                mRlvtSpeedFields.setVisibility(View.VISIBLE);
            }

            cancelRingDialog();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnStart:
                hideKeyBoard();
                String speedText = mEdtSpeed.getText().toString().trim();
                if (speedText.equals("") || speedText == null) {
                    Toast.makeText(this, "Enter speed in Km/hr", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mTempMarkerPoints.size() <= 0) {
                    mTempMarkerPoints = latLanModel.getArraLatLan();
                }

                mBtnStope.setEnabled(true);
                threadSleepPeriod = getTimeDuration();
               // Log.e(TAG, "threadSleepPeriod : " + threadSleepPeriod);
                startMarker = true;
                LatLng MovingMarkerLatLng = mMarkerPoints.get(1);
                animateMarker(mTempMarker, MovingMarkerLatLng, false);

                break;
            case R.id.btnStope:
                hideKeyBoard();
                startMarker = false;
                break;

            default:
                break;

        }
    }

    public double getTimeDuration() {

        String speedText = mEdtSpeed.getText().toString().trim();
        threadSleepPeriod = Double.parseDouble(String.valueOf(distance)) / Double.parseDouble(String.valueOf(speedText));
        threadSleepPeriod = threadSleepPeriod * 3600000;
        threadSleepPeriod = threadSleepPeriod / mTempMarkerPoints.size();

        return threadSleepPeriod;
    }


    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mGoogleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final LatLng[] movingLanLat = new LatLng[1];

        //Log.e(TAG, "Distance beetween to location is : " + mylocation.distanceTo(dest_location));

        /*To set duration
        * - Convert minutes into milisecond;
        * - 1 minute = 60000 millisecs.
        * */
        final long duration = 1 * 60000;
        ;

         /*SEt this value again if user click on start button after finising all the points*/
        if (mTempMarkerPoints.size() <= 0) {
            mTempMarkerPoints = latLanModel.getArraLatLan();
            movingLanLat[0] = mTempMarkerPoints.get(0);
            mPermantMarkerPoints.clear();
        }


        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);


                /*To set the marker*/
                for (int i = 0; i < mTempMarkerPoints.size(); i++) {

                    /*If stop button pressed then, need to break this loop*/
                    if (!startMarker) {
                        return;
                    }

                    mPermantMarkerPoints.add(mTempMarkerPoints.get(0));

                    movingLanLat[0] = mTempMarkerPoints.get(0);
                    marker.setPosition(mTempMarkerPoints.get(0));
                    mTempMarkerPoints.remove(0);

                    try {
                        Thread.sleep((long) threadSleepPeriod);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                latLanModel.setArraLatLan(mPermantMarkerPoints);
                if (mTempMarkerPoints.size() <= 0) {
                    t = 1;
                    mPermantMarkerPoints = new ArrayList<LatLng>();
                }

                if (mTempMarkerPoints.size() > 0) {
                    // Post again 16ms later (Delay time after completing each loop).
                    handler.postDelayed(this, -1);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo((float) getZoomLevel()));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(movingLanLat[0]));

            }
        });

    }


    private void drawMarker() {
        LatLng point;

        mGoogleMap.clear();
        for (int i = 0; i < mMarkerPoints.size(); i++) {
            point = mMarkerPoints.get(i);
            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(point);

            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            if (i == 0) {
                options.title(getLocationTittle(mAutocompleteTextView.getText().toString().trim()));
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                MarkerOptions tempOptions = new MarkerOptions();
                // Setting the position of the temp marker
                tempOptions.position(point);
                tempOptions.title(getLocationTittle(mAutocompleteTextView.getText().toString().trim()));
                tempOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mTempMarker = mGoogleMap.addMarker(new MarkerOptions().position(point));

            } else if (i == 1) {
                options.title(getLocationTittle(mAutocompleteTextView1.getText().toString().trim()));
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            }

            // Add new marker to the Google Map Android API V2
            mGoogleMap.addMarker(options);
        }

    }


    private String getLocationTittle(String locationString) {
        String[] parts = locationString.split(",");
        String part1 = parts[0];
        String part2 = parts[1];
        return part1;
    }



    @Override
    public void onLocationChanged(Location location) {
        // Draw the marker, if destination location is not set
        if (mMarkerPoints.size() < 2) {

            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            LatLng point = new LatLng(mLatitude, mLongitude);

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            cancelRingDialog();

        }
    }

    private double getZoomLevel() {
        double altitudeKm, zoomLevel;
        String heightValue = mEdtHeight.getText().toString().trim();
        altitudeKm = Double.parseDouble(heightValue);
        zoomLevel = 20 - log2(altitudeKm * 5.508);

        // Log.e(TAG, "Zoom Level : " + zoomLevel);
        return zoomLevel;
    }

    public static double log2(double a) {
        return logb(a, 2);
    }

    public static double logb(double a, double b) {
        return Math.log(a) / Math.log(b);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);


        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener1
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            Log.e(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback1);
            Log.e(LOG_TAG, "Fetching details for ID: " + item.placeId);

        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            Log.e(TAG, " getLatLng : " + place.getLatLng());

            if (mMarkerPoints.size() > 0) {
                mMarkerPoints.remove(0);
            }

            mStartPointLangLAt = place.getLatLng();
            // Adding new item to the ArrayList
            mMarkerPoints.add(0, place.getLatLng());

            //Log.e(TAG, " Length : " + mMarkerPoints.size());
            places.release();
        }
    };


    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback1
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);

            CharSequence attributions = places.getAttributions();

            Log.e(TAG, " getLatLng : " + place.getLatLng());

            // Adding new item to the ArrayList

            if (mMarkerPoints.size() <= 0) {
                Toast.makeText(MainActivity.this, getString(R.string.enter_valid_source), Toast.LENGTH_SHORT).show();
                mAutocompleteTextView.requestFocus();
                return;
            }

            if (mMarkerPoints.size() > 1) {
                mMarkerPoints.remove(1);
            }
            mMarkerPoints.add(1, place.getLatLng());
           // Log.e(TAG, " Length : " + mMarkerPoints.size());
            places.release();
        }
    };




    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
    }


    public void getBaloonView(final Marker baloon_marker, final LatLng toPosition,
                                  final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = mBaloonViewMarkerPoints.get(0);

        // final long duration = 50000;
        final long duration = (long) getTimeDurationForBaloonView();

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (!flagBaloonView) {
                    return;
                }

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;

                if (Double.parseDouble(String.valueOf(lat)) <= mMarkerPoints.get(1).latitude) {
                    if (Double.parseDouble(String.valueOf(lng)) <= mMarkerPoints.get(1).longitude) {

                        if (Double.parseDouble(String.valueOf(distance)) > 3.0) {
                            double markerPlacement = 0.00000005;
                            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo((float) getZoomLevel()));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mMarkerPoints.get(1).latitude - markerPlacement, mMarkerPoints.get(1).longitude - markerPlacement)));
                            baloon_marker.setPosition(new LatLng(mMarkerPoints.get(1).latitude - markerPlacement, mMarkerPoints.get(1).longitude - markerPlacement));

                            return;
                        } else {
                            baloon_marker.setPosition(new LatLng(lat, lng));
                            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo((float) getZoomLevel()));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                        }

                    }

                } else {
                    baloon_marker.setPosition(new LatLng(lat, lng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo((float) getZoomLevel()));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                }


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 0);
                } else {
                    if (hideMarker) {
                        baloon_marker.setVisible(false);
                    } else {
                        baloon_marker.setVisible(true);
                    }
                }


            }
        });
    }


    public double getTimeDurationForBaloonView() {

        String speedText = mEdtSpeed.getText().toString().trim();
        threadSleepPeriod = Double.parseDouble(String.valueOf(distance)) / Double.parseDouble(String.valueOf(speedText));
        threadSleepPeriod = threadSleepPeriod * 3600000;

        return threadSleepPeriod;
    }


    public double getShortestDistanceInKM(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.e(TAG, "Radius Value : " + "" + valueResult + " ,   KM : " + kmInDec
                + ", Meter: " + meterInDec);

        return valueResult;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enter_place:

                hideKeyBoard();

                /*SEt this false, to switch from  driving mode to baloon mode*/
                startMarker = false;
                flagBaloonView = false;


                mGoogleMap.clear();
                mMarkerPoints = new ArrayList<LatLng>();

                /*Clear all fields data*/
                mAutocompleteTextView.setText("");
                mAutocompleteTextView1.setText("");
                mEdtHeight.setText("");
                mEdtHeight.setText("");
                mEdtSpeed.setText("");

                mRlvtSpeedFields.setVisibility(View.GONE);
                mRelLayFields.setVisibility(View.VISIBLE);

                mBtnStope.setEnabled(false);

                return true;


            case R.id.baloon_view:

                hideKeyBoard();

                /*SEt this false, to switch from  driving mode to baloon mode*/
                startMarker = false;
                flagBaloonView = true;

                if (validation()) {

                    return true;
                } else if (mEdtSpeed.getText().toString().trim().equals("")) {
                    mGoogleMap.clear();
                    Toast.makeText(MainActivity.this, getString(R.string.enter_speed), Toast.LENGTH_SHORT).show();
                    mEdtSpeed.requestFocus();
                    return true;
                }

                drawMarker();

                mRelLayFields.setVisibility(View.GONE);
                mRlvtSpeedFields.setVisibility(View.GONE);

                LatLng MovingMarkerLatLng = mBaloonViewMarkerPoints.get(1);
                mTempMarker.setPosition(mBaloonViewMarkerPoints.get(0));
                distance = "" + getShortestDistanceInKM(mBaloonViewMarkerPoints.get(0), mBaloonViewMarkerPoints.get(1));

                getBaloonView(mTempMarker, MovingMarkerLatLng, false);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void launchRingDialog(String progressMessage) {
        ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait...",progressMessage , true);
        ringProgressDialog.setCancelable(false);
    }

    public void cancelRingDialog() {
        ringProgressDialog.dismiss();
    }


    private Animation.AnimationListener animationListener = new Animation.AnimationListener(){

        @Override
        public void onAnimationEnd(Animation animation) {
            //mRlvtInternetConection.setVisibility(View.GONE);

            if (!internetStatus)
                mRlvtInternetConection.setVisibility(View.GONE);
            else
                mRlvtInternetConection.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub

        }};
}
