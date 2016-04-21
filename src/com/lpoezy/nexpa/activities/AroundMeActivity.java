package com.lpoezy.nexpa.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;

import com.appyvet.rangebar.RangeBar;
import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lpoezy.nexpa.JSON.JSONParser;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.Geolocation;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.objects.Users;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.HttpUtilz;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class AroundMeActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnRefreshListener, Correspondent.OnCorrespondentUpdateListener, XMPPService.OnServiceConnectedListener {
    private static final String TAG = AroundMeActivity.class.getSimpleName();
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private static final String LOCATION_KEY = "LOCATION_KEY";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "LAST_UPDATED_TIME_STRING_KEY";
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = -1;
    // Button btnUpdate;
    LocationManager locationManager;
    // MyLocationListener locationListener;
    float ftLatitude = 0;
    float ftLongitude = 0;
    Handler mHandler;
    GridView grid;
    CustomGrid adapter;
    //SQLiteHandler db;
    float i = 0;
    ArrayList<String> web = new ArrayList<String>();
    ArrayList<String> availabilty = new ArrayList<String>();
    ArrayList<Integer> distance = new ArrayList<Integer>();
    ArrayList<Integer> imageId = new ArrayList<Integer>();
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    ArrayList<Correspondent> arr_correspondents = new ArrayList<Correspondent>();
    // ArrayList<String> arr_fname = new ArrayList<String>();
    //ArrayList<String> arr_age = new ArrayList<String>();
    ArrayList<String> arr_uname = new ArrayList<String>();
    //ArrayList<String> arr_gender = new ArrayList<String>();
    //ArrayList<String> arr_looking_type = new ArrayList<String>();
    ArrayList<String> arr_date_seen = new ArrayList<String>();
    //ArrayList<String> arr_about = new ArrayList<String>();
    //ArrayList<String> arr_email = new ArrayList<String>();
    //ArrayList<String> arr_status = new ArrayList<String>();

    //ArrayList<Users> us = new ArrayList<Users>();
    ArrayList<Geolocation> nearbyUsers = new ArrayList<Geolocation>();

    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean gps_enabled = false;
    boolean network_enabled = false;
    boolean hasGps = false;
    float longitude = 0;
    float latitude = 0;
    String gpsProvider = "";

    String ins_user = "";
    float ins_latitude = 0;
    float ins_longitude = 0;
    //String existingUsers;

    DateUtils du;
    private AsyncTask<String, String, String> mTask;

    JSONParser jsonParser = new JSONParser();

    private static final String TAG_SUCCESS = "success";

    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> userList;
    JSONArray nearby_users = null;

    // JSON Node names
    private static final String TAG_GEO_SUCCESS = "success";

    private static final String TAG_GEO = "geo";
    private static final String TAG_GEO_PID = "id";
    private static final String TAG_GEO_USER = "user";
    private static final String TAG_GEO_LATITITUDE = "latitude";
    private static final String TAG_GEO_LONGI = "longitude";
    private static final String TAG_GEO_PROVIDER = "gps_provider";
    private static final String TAG_GEO_DATE_CREATE = "date_create";
    private static final String TAG_GEO_FNAME = "firstname";
    private static final String TAG_GEO_LNAME = "lastname";
    private static final String TAG_GEO_BIRTHDAY = "birthday";
    private static final String TAG_GEO_GENDER = "gender";
    private static final String TAG_GEO_DISTANCE = "geo_distance";

    private static final String TAG_GEO_ABOUTME = "about_me";
    private static final String TAG_GEO_LOOKING_TYPE = "looking_type";
    private static final String TAG_GEO_STATUS = "status";

    private static final String TAG_GEO_EMAIL = "email_address";
    // private static final int DEFAULT_TICK_START = 1;

    public static boolean isRunning = false;
    private int dst;
    private int oldDst;
    private String mUsername;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    Dialog dialogPref;
    RangeBar rbDistance;
    EditText rbDistance1;
    String distTick = "";
    private boolean mIsSuperuser;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case R.id.action_distance:


                dialogPref = new Dialog(AroundMeActivity.this);
                dialogPref.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogPref.setContentView(R.layout.activity_profile_distance_settings);

                rbDistance = (RangeBar) dialogPref.findViewById(R.id.rbDistance);
                rbDistance.setRangeBarEnabled(false);

                SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
                db.openToRead();
                try {
                    dst = Integer.parseInt(db.getBroadcastDist());
                } catch (Exception e) {
                    dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
                }
                L.debug("db.getBroadcastDist()" + db.getBroadcastDist());
                db.close();

                rbDistance.setSeekPinByValue(dst);

                rbDistance.setPinColor(getResources().getColor(R.color.EDWARD));
                rbDistance.setConnectingLineColor(getResources().getColor(R.color.EDWARD));
                rbDistance.setSelectorColor(getResources().getColor(R.color.EDWARD));
                rbDistance.setPinRadius(30f);
                rbDistance.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
                    @Override
                    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
                                                      String leftPinValue, String rightPinValue) {
                        distTick = rightPinValue;
                    }
                });

                Button dialogButton = (Button) dialogPref.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
                        db.openToWrite();

                        db.updateBroadcastDist(distTick);
                        db.close();
                        try {
                            dst = Integer.parseInt(distTick);

                            //tryGridToUpdate();

                        } catch (NumberFormatException e) {
                            dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
                        }

                        if (mBounded) {
                            mService.onExecutePendingTask(new OnDownloadNearbyUsersOnline());
                        }
                        //downloadNearbyUsersOnline();
                        dialogPref.dismiss();
                    }
                });

                CheckBox cbxSuperUser = (CheckBox) dialogPref.findViewById(R.id.cbx_superuser);
                SessionManager sm = new SessionManager(AroundMeActivity.this);
                cbxSuperUser.setChecked(sm.isSuperuser());

                cbxSuperUser.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SessionManager sm = new SessionManager(AroundMeActivity.this);
                        if (((CheckBox) v).isChecked()) {
                            rbDistance.setEnabled(false);
                            sm.setSuperuser(true);
                        } else {
                            sm.setSuperuser(false);
                            rbDistance.setEnabled(true);
                        }

                    }
                });

                if (cbxSuperUser.isChecked()) {
                    rbDistance.setEnabled(false);
                } else {
                    rbDistance.setEnabled(true);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialogPref.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogPref.show();
                dialogPref.getWindow().setAttributes(lp);


                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }


    }

    protected void onStart() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
        // locationManager.removeUpdates(locationListener);
        isRunning = false;
        //stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

        mRequestingLocationUpdates = false;
    }


    @Override
    protected void onResume() {

        super.onResume();
        isRunning = true;

        SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
        db.openToRead();
        try {
            dst = Integer.parseInt(db.getBroadcastDist());
        } catch (Exception e) {
            dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
        }

        if (oldDst != dst) {
            // force grid update when new distance detected
            //tryGridToUpdate();
            oldDst = dst;
        }

        db.close();

        if (mBounded) {

        }

    }


    private Location mCurrentLocation;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        mRequestingLocationUpdates = true;
    }

    private class OnDownloadNearbyUsersOnline implements OnExecutePendingTaskListener {

        @Override
        public void onExecutePendingTask() {

            //mSwipeRefreshLayout.setRefreshing(true);

            if (!XMPPService.xmpp.connection.isConnected()) {


                XMPPManager.getInstance(AroundMeActivity.this).instance = null;

                XMPPService.xmpp = XMPPManager.getInstance(AroundMeActivity.this);

                XMPPService.xmpp.connect("onCreate");

            } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                XMPPService.xmpp.login();
            } else {


                //*
                if (mCurrentLocation == null) {
                    L.error("mCurrentLocation is null!!!");
                    return;
                }

                L.debug("AroundMe, downloadNearbyUsersOnline");
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        HashMap<String, String> postDataParams = new HashMap<String, String>();

                        SessionManager sm = new SessionManager(AroundMeActivity.this);
                        int newDistance = sm.isSuperuser() ? AppConfig.SUPERUSER_MAX_DISTANCE_KM : dst;

                        postDataParams.put("tag", "download_nearby_users");
                        postDataParams.put("username", mUsername);
                        postDataParams.put("longitude", mCurrentLocation.getLongitude() + "");
                        postDataParams.put("latitude", mCurrentLocation.getLatitude() + "");

                        postDataParams.put("dist", newDistance + "");
                        postDataParams.put("unit", "k");


                        final String spec = AppConfig.URL_GEO;
                        String webpage = HttpUtilz.makeRequest(spec, postDataParams);

                        L.debug(webpage);

                        updateUI(webpage);

                    }
                }).start();
                //*/
            }

        }
    }

    ;


    private void updateUI(final String webpage) {


        try {
            JSONObject jObj = new JSONObject(webpage);
            boolean error = jObj.getBoolean("error");
            if (!error) {


                nearby_users = jObj.getJSONArray("data");


                Log.e("LOG", "*****JARRAY*****" + nearby_users.length());
                //db.deleteAllPeople();
                ///////////////////////
                if (nearby_users.length() == 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });


                } else {
                    for (int i = 0; i < nearby_users.length(); i++) {
                        JSONObject c = nearby_users.getJSONObject(i);

                        // user profile

                        String uname = c.getString("username");
                        String latitude = c.getString("latitude");
                        String longitude = c.getString("longitude");
                        String gps_provider = c.getString("gps_provider");
                        String date_create = c.getString("date_create");
                        String date_update = c.getString("date_update");
                        String geo_distance = c.getString("geo_distance");

                        SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
                        db.openToWrite();
                        db.insertNearbyUser(uname, latitude, longitude, gps_provider, date_create, date_update, geo_distance);
                        db.close();
                    }

                    updateGrid();
                }
            } else {
                //makeNotify("Error occurred while collecting users", AppMsg.STYLE_ALERT);
                L.makeText(AroundMeActivity.this, "Error occurred while collecting users", AppMsg.STYLE_ALERT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void sendNewLocToServer(final double lat, final double longi) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                Geolocation geo = new Geolocation();
                geo.setUsername(mUsername);
                geo.setLatitude(lat);
                geo.setLongitude(longi);

                geo.saveOnline();


            }
        }).start();


    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mCurrentLocation != null) {
            //send new location to server

            L.debug("last loc latitude: " + String.valueOf(mCurrentLocation.getLatitude()));
            L.debug("last loc long: " + String.valueOf(mCurrentLocation.getLongitude()));
            if (mBounded && nearbyUsers.size() == 0) {

                mService.onExecutePendingTask(new OnDownloadNearbyUsersOnline());

                //downloadNearbyUsersOnline();
            }

            sendNewLocToServer(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        //L.debug("last loc change latitude: "+String.valueOf(mCurrentLocation.getLatitude()));
        //L.debug("last loc change long: "+String.valueOf(mCurrentLocation.getLongitude()));
        //L.debug("last update time: "+mLastUpdateTime);

        //send new location to server
        if (mCurrentLocation != null) {
            sendNewLocToServer(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        L.makeText(this, "onConnectionSuspended", AppMsg.STYLE_ALERT);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                connectionResult.getErrorCode(),
                this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = ErrorDialogFragment.newInstance(errorDialog);
                    /*new ErrorDialogFragment()*/
            ;
            // Set the dialog in the DialogFragment
            //errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(),
                    "Location Updates");
        }


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);


        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);

            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    private boolean mFrmSaveInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_around_me);

        if (savedInstanceState != null) mFrmSaveInstanceState = true;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.drawable.icon_nexpa);
        myToolbar.setTitle("");

//        db = new SQLiteHandler(AroundMeActivity.this);
//        db.openToWrite();
        SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
        db.openToRead();
        mUsername = db.getUsername();
        db.close();
        oldDst = 0;


//
//		du = new DateUtils();
//		db = new SQLiteHandler(this);
//		db.openToWrite();
//		userList = new ArrayList<HashMap<String, String>>();
//
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.niagara, R.color.buttercup, R.color.niagara);
        mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.carrara));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (mBounded) {
                            mService.onExecutePendingTask(new OnDownloadNearbyUsersOnline());
                        }
                        //downloadNearbyUsersOnline();
                    }
                }, 500);
            }
        });


        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        }, 500);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
//
//		// web.add(0, "You");
//		// distance.add(0, 0);
//		// imageId.add(0, R.drawable.pic_sample_girl);
//		// availabilty.add(0, "Online");
        adapter = new CustomGrid(AroundMeActivity.this, web, arr_correspondents/* imageId */, availabilty, distance);


//
        grid = (GridView) findViewById(R.id.grid);

        grid.setAdapter(adapter);
        grid.setBackgroundColor(Color.WHITE);
        grid.setVerticalSpacing(1);
        grid.setHorizontalSpacing(1);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Users> us = new ArrayList<Users>();
                try {
                    // Toast.makeText(AroundMeActivity.this, "You Clicked at "
                    // +arr_fname.get(position) , Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AroundMeActivity.this, PeopleProfileActivity.class);

                    // intent.putExtra("TAG_GEO_PID", us.get(position).getId());
                    //long correspondentId = arr_correspondents.get(position).getId();


                    intent.putExtra("TAG_GEO_USER", nearbyUsers.get(position).getUsername());
                    String distance = String.format(
                            new DecimalFormat("#.###").format(nearbyUsers.get(position).getDistance() * 0.621));

                    // L.debug("distance: "+distance);
                    intent.putExtra("TAG_GEO_DISTANCE", distance);

                    startActivity(intent);
                } catch (Exception e) {
                }
                // finish();
            }
        });

    }

    private void updateGrid() {
        L.debug("updateGrid");

        // us = null;
        //us = new ArrayList<Geolocation>();

        SQLiteHandler db = new SQLiteHandler(AroundMeActivity.this);
        db.openToRead();

        int dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;

        try {
            dst = Integer.parseInt(db.getBroadcastDist());
        } catch (Exception e) {
            dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
        }


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                grid.invalidateViews();

            }
        });


        nearbyUsers = db.getNearByUserDetails();

        imageId.clear();
        availabilty.clear();
        web.clear();
        distance.clear();

        Animation in = AnimationUtils.loadAnimation(AroundMeActivity.this, R.anim.anim_fade_in_r);
        Animation out = AnimationUtils.loadAnimation(AroundMeActivity.this, R.anim.anim_fade_out_r);
        arr_correspondents.clear();


        final Roster roster = Roster.getInstanceFor(XMPPService.xmpp.connection);

        for (int j = 0; j < nearbyUsers.size(); j++) {
            try {
                final String name = nearbyUsers.get(j).getUsername();
                final Correspondent correspondent = new Correspondent();
                correspondent.setUsername(name);
                correspondent.addListener(AroundMeActivity.this);

                arr_correspondents.add(j, correspondent);


                final String address = name + "@198.154.106.139/Smack";



                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            requestSubscription(address);
                        } catch (SmackException.NotConnectedException e) {
                           L.error(""+e.getMessage());
                        } catch (SmackException.NotLoggedInException e) {
                            L.error("" + e.getMessage());
                        } catch (SmackException.NoResponseException e) {
                            L.error("" + e.getMessage());
                        } catch (XMPPException.XMPPErrorException e) {
                            L.error("" + e.getMessage());
                        }
                        updateUserAvailability(address, roster);

                        final UserProfile uProfile = new UserProfile();
                        uProfile.setUsername(name);

                        uProfile.loadVCard(XMPPService.xmpp.connection);

                        L.debug("updateGrid, uname: " + uProfile.getUsername() + ", desc: " + uProfile.getDescription() + ", " + uProfile.getAvatarImg());

                        updateUserAvatar(name, uProfile.getAvatarImg());


                    }
                }).start();


                imageId.add(j, R.drawable.pic_sample_girl);
                availabilty.add(j, "ADDED");
                web.add(j, name);
            } catch (IndexOutOfBoundsException e) {
                L.error(e.getMessage());
            }

        }

        subscriptionRequestListener();

        roster.addRosterListener(new RosterListener() {
            // Ignored events public void entriesAdded(Collection<String> addresses) {}
            public void entriesDeleted(Collection<String> addresses) {
            }

            @Override
            public void entriesAdded(Collection<String> collection) {

            }

            public void entriesUpdated(Collection<String> addresses) {
            }

            public void presenceChanged(Presence presence) {
                //L.debug("Presence changed: " + presence.getFrom() + " " + presence);
                L.debug("Presence changed: " + presence.getFrom());
                updateUserAvailability(presence.getFrom(), roster);

            }
        });



        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        db.close();
    }

    private String displayGridCellName(String fname, String user) {

        if (fname.equals("")) {
            return user;
        } else {
            return fname;
        }
    }

    private String displayAge(String age) {
        if (age.length() < 4) {
            return age;
        } else {
            return "";
        }
    }

    private void makeNotify(CharSequence con, Style style) {
        AppMsg.makeText(this, con, style).show();
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub

    }

    private void updateUserAvatar(final String addresss, Bitmap avatar) {

        String username = addresss;
        for (Correspondent c : arr_correspondents) {

            if (c.getUsername().equals(username)) {


                c.setProfilePic(avatar);
            }
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {


                adapter.notifyDataSetChanged();
            }
        });


    }

    private void updateUserAvailability(final String addresss, final Roster roster) {
        // L.debug("updateUserAvailability: "+arr_correspondents.size());
        String username = addresss.split("@")[0];
        for (Correspondent c : arr_correspondents) {

            if (c.getUsername().equals(username)) {

                boolean isAvailable = roster.getPresence(addresss).isAvailable();
                L.debug(addresss + " isAvailable? " + isAvailable);
                c.setAvailable(isAvailable);

            }
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });


    }


    @Override
    public void onCorrespondentUpdate() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }

    private XMPPService mService;
    private boolean mBounded;

    @Override
    public void OnServiceConnected(XMPPService service) {
        mService = service;
        mBounded = true;

        if (nearbyUsers != null && nearbyUsers.size() == 0) {
            mService.onExecutePendingTask(new OnDownloadNearbyUsersOnline());
        }

    }

    @Override
    public void OnServiceDisconnected() {
        mService = null;
        mBounded = false;


    }

    private void requestSubscription(String address) throws SmackException.NotConnectedException, SmackException.NotLoggedInException, SmackException.NoResponseException, XMPPException.XMPPErrorException {

        //L.error("sending subscription request to address: " + address);
        Presence subscribe = new Presence(Presence.Type.subscribe);
        subscribe.setTo(address);
        XMPPService.xmpp.connection.sendPacket(subscribe);

        final Roster roster = Roster.getInstanceFor(XMPPService.xmpp.connection);

        roster.createEntry(address, null, null);


    }

    private void subscriptionRequestListener() {

        L.debug("==============subscriptionRequestListener================");
        XMPPService.xmpp.connection.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
                L.debug("==============subscriptionRequestListener processPacket================");
                final Presence presence = (Presence) stanza;
                final String fromId = presence.getFrom();
                //final RosterEntry newEntry = connection.getRoster().getEntry(fromId);
                final String uname = fromId.split("@")[0];

//                Correspondent correspondent = null;
//                for (Correspondent c : arr_correspondents) {
//                    if (c.getUsername().equals(uname)) {
//                        correspondent = c;
//                        break;
//                    }
//                }
                // Correspondent correspondent = arr_correspondents.;

                if (presence.getType() == Presence.Type.subscribe) {

                    L.debug("subscribe: " + fromId);
                    //approved request
                    Presence subscribed = new Presence(Presence.Type.subscribed);
                    subscribed.setTo(fromId);
                    XMPPService.xmpp.connection.sendPacket(subscribed);

                } else if (presence.getType() == Presence.Type.unsubscribe) {
                    L.debug("unsubscribe: " + fromId);
                } else if (presence.getType() == Presence.Type.subscribed) {
                    L.debug("subscribed: " + fromId);
                } else if (presence.getType() == Presence.Type.unsubscribed) {
                    L.debug("unsubscribed: " + fromId);
                } else if (presence.getType() == Presence.Type.available) {
                    L.debug("available: " + fromId);

                } else if (presence.getType() == Presence.Type.unavailable) {
                    L.debug("unavailable: " + fromId);

                }
            }
        }, new PacketTypeFilter(Presence.class));
        L.debug("================================");

    }
}