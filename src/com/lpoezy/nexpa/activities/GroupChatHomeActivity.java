package com.lpoezy.nexpa.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.devspark.appmsg.AppMsg;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Announcement;
import com.lpoezy.nexpa.objects.Geolocation;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.DividerItemDecoration;
import com.lpoezy.nexpa.utility.HttpUtilz;
import com.lpoezy.nexpa.utility.L;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GroupChatHomeActivity extends AppCompatActivity implements XMPPService.OnServiceConnectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = -1;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private static final String LOCATION_KEY = "LOCATION_KEY";
    Button btnStartChat;
    Button btnCancel;
    Button dialogButtonOK;
    EditText edBroad;
    String mNickName;
    //SQLiteHandler db;
    Message msg;
    String strUser;
    int repeater;
    int interactor;
    LinearLayout lnBroadcast;
    LinearLayout lnBroadcastMini;
    static LinearLayout lnEmpty;
    LinearLayout lnBroadcastExist;
    LinearLayout btnReply;
    LinearLayout btnFave;
    LinearLayout btnDel;
    TextView txBroad;
    Dialog dialogBroadcast;
    Button btnOptions;
    Button btnRefresher;
    static TextView txtConnection;
    static Animation animFade;

    TextView txtReply;
    TextView txtUser;

    DateUtils du;
    ListView mListView;
    //SimpleCursorAdapter mAdapter;
    Handler mHandler;
    Handler mNotifier;
    Handler mRepeater;
    Runnable mStatusChecker;
    String senderEdited;
    String messageToSend;
    boolean isReceivingBroadcast;
    Cursor crBroadcast;

    boolean flag_loading;
    int limit_loader;
    int broadCount;
    int limit_listen_maker;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private String mAddress;
    private ImageView btnDistance;
    private Dialog dialogPref;
    private RangeBar rbDistance;
    private int dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
    private String distTick;
    private String mUsername;
    private RecyclerView mRvBroadcasts;
    public static List<Announcement> announcements;
    private BroadcastAdapter mAdapter;
    public static boolean isRunning;
    private static boolean frmOnSavedInstance;
    private static boolean frmPause;
    private SwipyRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

        mRequestingLocationUpdates = false;
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        if(mService!=null){
            mService = null;
            mBound =false;
        }

        XMPPService.xmpp.removeUpdateBroadcastUIListener(mOnupdateUI);
        frmPause = true;
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onStart() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (((TabHostActivity) getParent()).getService()!=null) {
          mService = ((TabHostActivity) getParent()).getService();
            mBound =true;
        }

        if (XMPPService.xmpp != null)
            XMPPService.xmpp.registerUpdateBroadcastUIListener(mOnupdateUI);
/*/
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (XMPPService.xmpp.connection != null && XMPPService.xmpp.connection.isAuthenticated()) {


                    SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
                    db.openToRead();
                    // Create a pubsub manager using an existing XMPPConnection
                    PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                    LeafNode node = null;
                    try {

                        ConfigureForm form = new ConfigureForm(DataForm.Type.form.submit);
                        form.setAccessModel(AccessModel.open);
                        form.setDeliverPayloads(true);
                        form.setNotifyRetract(true);
                        form.setPersistentItems(true);
                        form.setPublishModel(PublishModel.open);

                        node = (LeafNode) mgr.createNode(db.getUsername() + "-broadcast", form);

                    } catch (SmackException.NoResponseException e) {
                        L.error(e.getMessage());
                    } catch (XMPPException.XMPPErrorException e) {
                        L.error(e.getMessage());
                    } catch (SmackException.NotConnectedException e) {
                        L.error(e.getMessage());
                    }
                    //PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);
                    try {
                        node = mgr.getNode(db.getUsername() + "-broadcast");

                        //node.addItemEventListener(new ItemEventCoordinator());

                        node.subscribe(db.getUsername() + "@198.154.106.139");
                    } catch (SmackException.NoResponseException e) {
                        L.error(e.getMessage());
                    } catch (XMPPException.XMPPErrorException e) {
                        L.error(e.getMessage());
                    } catch (SmackException.NotConnectedException e) {
                        L.error(e.getMessage());
                    }


                    db.close();
                }
            }
        }).start();
//*/
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        isRunning = true;
    }


    private  void addNewAnnouncement(final Announcement ann) {


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //L.debug("itemId: "+ann.getItemId());
                boolean doAdd = true;
                for (Announcement announcement : announcements) {
                    if (announcement.getItemId().equals(ann.getItemId())) {

                        doAdd = false;
                    }
                }

                if (doAdd) {
                    // int size = announcements.size()-1<0?0:announcements.size()-1;
                    lnEmpty.setEnabled(false);
                    lnEmpty.setVisibility(LinearLayout.GONE);

                    final String uname = ann.getFrom();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final UserProfile userProfile = new UserProfile();
                            userProfile.setUsername(uname);
                            userProfile.loadVCard(XMPPService.xmpp.connection);

                            if (userProfile.getAvatarImg() != null) {

                                ann.setDP(userProfile.getAvatarImg());

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        mAdapter.notifyDataSetChanged();

                                    }
                                });


                            }


                        }
                    }).start();

                    int index = announcements != null && !announcements.isEmpty() ? announcements.size() - 1 : 0;

                    announcements.add(0, ann);
                    //Collections.reverse(announcements);
                    mAdapter.notifyDataSetChanged();






                }
            }
        });

    }

    private OnUpdateUIListener mOnupdateUI = new OnUpdateUIListener() {
        @Override
        public void onUpdateUI() {
            downloadReceivedBroadcastsNearby();
        }
    };

    private class BroadcastAdapter extends RecyclerView.Adapter<BroadcastAdapter.ViewHolder> {

        private final Context context;
        private LayoutInflater inflater;
        // private List<Correspondent> buddys;

        public BroadcastAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            // this.buddys = buddys;
            this.context = context;

        }

        @Override
        public int getItemCount() {

            return announcements.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder vh, final int position) {

            SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
            db.openToRead();
            String uname = db.getUsername();
            db.close();

            Announcement ann = announcements.get(position);

            boolean isMine = uname.equals(ann.getFrom());

            vh.tvBroadId.setText(ann.getItemId());
            vh.tvBroadMsg.setText(ann.getBody());
            vh.ImgReply.setImageResource(R.drawable.btn_reply);
            vh.tvReply.setText("REACHED " + ann.getReach());

            vh.ImgReply.setVisibility(!isMine ? View.VISIBLE : View.GONE);

            vh.tvReply.setVisibility(isMine ? View.VISIBLE : View.GONE);
            vh.tvBroadFrm.setText(ann.getFrom());


            DateUtils du = new DateUtils();
            String dateFormatted = "";
            if (ann.getDate() != null && !ann.getDate().isEmpty()) {

                vh.tvDateBroad.setText(DateUtils.millisToSimpleDate(Long.parseLong(ann.getDate()), DateUtils.DateFormatz.DATE_FORMAT_5));
            }

            vh.tvLocLocal.setVisibility(View.GONE);

            if (ann.getLocLocal() != null && !ann.getLocLocal().isEmpty()) {
                String strLoc = "near " + ann.getLocLocal();

                vh.tvLocLocal.setText(strLoc);
                vh.tvLocLocal.setVisibility(TextView.VISIBLE);
            }
            vh.imgProfile.setImageResource(R.drawable.pic_sample_girl);
            if (ann.getDP() != null) {
                vh.imgProfile.setImageBitmap(ann.getDP());
            }

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    0, ActionBar.LayoutParams.WRAP_CONTENT, 1.2f);
            vh.btnReply.setLayoutParams(param);


        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = inflater.inflate(R.layout.list_broadcast, parent, false);
            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final CircularImageView imgProfile;
            TextView tvBroadId;
            TextView tvBroadFrm;
            TextView tvDateBroad;
            TextView tvLocLocal;
            TextView tvBroadMsg;
            TextView tvReach;
            TextView tvReply;
            ImageView ImgReply;
            LinearLayout btnReply;
            TextView tvBroadFrmRaw;

            public ViewHolder(View itemView) {
                super(itemView);
                imgProfile = (CircularImageView) itemView.findViewById(R.id.img_profile);
                tvBroadId = (TextView) itemView.findViewById(R.id.broad_id);
                tvBroadFrm = (TextView) itemView.findViewById(R.id.broad_from);
                tvDateBroad = (TextView) itemView.findViewById(R.id.date_broad);
                tvLocLocal = (TextView) itemView.findViewById(R.id.location_local);
                tvBroadMsg = (TextView) itemView.findViewById(R.id.broad_message);
                tvReach = (TextView) itemView.findViewById(R.id.reach);
                tvReply = (TextView) itemView.findViewById(R.id.txtReply);
                ImgReply = (ImageView) itemView.findViewById(R.id.imgReply);
                btnReply = (LinearLayout) itemView.findViewById(R.id.btnReply);
                tvBroadFrmRaw = (TextView) itemView.findViewById(R.id.broad_from_raw);

                itemView.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
                db.openToRead();
                String uname = db.getUsername();
                db.close();
//                if (tvBroadFrm.getText().toString().equals(uname)) {
//                    return;
//                }

                Intent intent = new Intent(GroupChatHomeActivity.this, CommentsActivity.class);
                String locLocal = (mAddress != null && !mAddress.isEmpty()) ? mAddress : "";
                intent.putExtra(CommentsFragment.UNAME, tvBroadFrm.getText().toString());
                intent.putExtra(CommentsFragment.BROADCAST_ID, tvBroadId.getText().toString());
                intent.putExtra(CommentsFragment.ADDRESS, tvLocLocal.getText().toString());
                intent.putExtra(CommentsFragment.DATE, tvDateBroad.getText().toString());
                intent.putExtra(CommentsFragment.BROADCAST, tvBroadMsg.getText().toString());
                startActivity(intent);
            }
        }
    }


    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_home);

        mRvBroadcasts = (RecyclerView) findViewById(R.id.rv_my_broadcasts);
        mRvBroadcasts.setLayoutManager(new LinearLayoutManager(GroupChatHomeActivity.this));
        mRvBroadcasts.addItemDecoration(new DividerItemDecoration(GroupChatHomeActivity.this, DividerItemDecoration.VERTICAL_LIST));
        announcements = new ArrayList<Announcement>();
        mAdapter = new BroadcastAdapter(GroupChatHomeActivity.this);
        mRvBroadcasts.setAdapter(mAdapter);


        lnBroadcast = (LinearLayout) findViewById(R.id.lnBroadcast);
        lnEmpty = (LinearLayout) findViewById(R.id.lnBroadcastEmpty);
        lnBroadcastExist = (LinearLayout) findViewById(R.id.lnBroadcastExist);
        //lnBroadcastExist.setOnClickListener(mBuyButtonClickListener);
        lnEmpty.setEnabled(false);
        lnEmpty.setVisibility(LinearLayout.GONE);

        lnBroadcastMini = (LinearLayout) findViewById(R.id.lnBroadcastMini);
        dialogBroadcast = new Dialog(GroupChatHomeActivity.this);
        dialogBroadcast.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBroadcast.setContentView(R.layout.activity_group_chat_main);
        edBroad = (EditText) dialogBroadcast.findViewById(R.id.txtBroadcast);
        btnCancel = (Button) dialogBroadcast.findViewById(R.id.btnClose);
        btnDistance = (ImageView) findViewById(R.id.img_here);
        //btnRefresher = (Button) findViewById(R.id.btnOptions);
        //btnOptions = (Button) findViewById(R.id.btnOptions);
        txtConnection = (TextView) findViewById(R.id.txt_broad_stat);
        txtConnection.setText("Connected To Server");
        txtConnection.setVisibility(View.GONE);
        mGson = new Gson();
        mListView = (ListView) findViewById(R.id.listview);

        animFade = AnimationUtils.loadAnimation(GroupChatHomeActivity.this, R.anim.anim_fade_in_r);

        SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
        db.openToRead();
        mUsername = db.getUsername();
        db.close();


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogBroadcast.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogBroadcast.getWindow().setAttributes(lp);

        lnBroadcastMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(lnBroadcast.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                openBroadcastDialog();

            }
        });


        btnDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                openDistanceDialog();


            }


        });


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();

        mSwipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.niagara, R.color.buttercup, R.color.niagara);
        mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.carrara));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {

                downloadReceivedBroadcastsNearby();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void openDistanceDialog() {

        dialogPref = new Dialog(GroupChatHomeActivity.this);
        dialogPref.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPref.setContentView(R.layout.activity_profile_distance_settings);

        rbDistance = (RangeBar) dialogPref.findViewById(R.id.rbDistance);
        rbDistance.setRangeBarEnabled(false);


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
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    dst = Integer.parseInt(distTick);

                } catch (NumberFormatException e) {
                    dst = AppConfig.SUPERUSER_MIN_DISTANCE_KM;
                }

                dialogPref.dismiss();
            }
        });

        CheckBox cbxSuperUser = (CheckBox) dialogPref
                .findViewById(R.id.cbx_superuser);
        SessionManager sm = new SessionManager(
                GroupChatHomeActivity.this);
        cbxSuperUser.setChecked(sm.isSuperuser());

        cbxSuperUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SessionManager sm = new SessionManager(
                        GroupChatHomeActivity.this);
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

        }
    }

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.lpoezy.nexpa";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

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

    private void openBroadcastDialog() {
        edBroad.setText("");
        dialogBroadcast.show();
        btnStartChat = (Button) dialogBroadcast.findViewById(R.id.btnStartLocChat);
        btnStartChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                final String msgToBroadcast = edBroad.getText().toString();


                //get nearby users
                final ProgressBar pbBroadcast = (ProgressBar) dialogBroadcast.findViewById(R.id.pbBroadcast);
                pbBroadcast.post(new Runnable() {
                    @Override
                    public void run() {
                        pbBroadcast.setVisibility(View.VISIBLE);
                    }
                });

                OnBroadcastMessageTask broadcastMessage = new OnBroadcastMessageTask(GroupChatHomeActivity.this, msgToBroadcast);
                L.debug("mService: "+mService);
                //broadcastMessage(msgToBroadcast);
                if(mService!=null){
                    mService.onExecutePendingTask(broadcastMessage);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        dialogBroadcast.dismiss();
                    }
                });


            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dialogBroadcast.dismiss();
            }
        });
    }

    private List<Geolocation> mNearbyUsers = new ArrayList<Geolocation>();

    private void downloadNearbyUsers() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {

        L.debug("GroupChatHome, downloadNearbyUsers");
        HashMap<String, String> postDataParams = new HashMap<String, String>();

        SessionManager sm = new SessionManager(GroupChatHomeActivity.this);
        int newDistance = sm.isSuperuser() ? AppConfig.SUPERUSER_MAX_DISTANCE_KM : dst;

        postDataParams.put("tag", "download_nearby_users");
        postDataParams.put("username", mUsername);
        postDataParams.put("longitude", mCurrentLocation.getLongitude() + "");
        postDataParams.put("latitude", mCurrentLocation.getLatitude() + "");

        postDataParams.put("dist", newDistance + "");
        postDataParams.put("unit", "k");

        final String spec = AppConfig.URL_GEO;
        String webpage = HttpUtilz.makeRequest(spec, postDataParams);

        L.debug("webpage: " + webpage);

        JSONObject jObj = null;
        try {
            jObj = new JSONObject(webpage);
        } catch (JSONException e) {
            L.error(e.getMessage());
        }
        boolean error = false;
        try {
            error = jObj.getBoolean("error");
        } catch (JSONException e) {
            L.error(""+e.getMessage());
        } catch (NullPointerException e) {
            L.error(""+e.getMessage());
        }

        if (!error) {

            JSONArray nearby_users = null;
            try {
                nearby_users = jObj.getJSONArray("data");
            } catch (JSONException e) {
                L.error(""+e.getMessage());
            }

            if (nearby_users != null && nearby_users.length() != 0) {
                mNearbyUsers.clear();
                for (int i = 0; i < nearby_users.length(); i++) {

                    try {

                        JSONObject c = nearby_users.getJSONObject(i);
                        // user profile
                        String uname = c.getString("username");
                        String latitude = c.getString("latitude");
                        String longitude = c.getString("longitude");
                        String gps_provider = c.getString("gps_provider");
                        String date_create = c.getString("date_create");
                        String date_update = c.getString("date_update");
                        String geo_distance = c.getString("geo_distance");
                        Geolocation geo = new Geolocation(uname, Double.parseDouble(latitude), Double.parseDouble(longitude),
                                gps_provider, Long.parseLong(date_create), Long.parseLong(date_update), Double.parseDouble(geo_distance));

//                                final SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
//                                db.openToRead();
//
//                                PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);
//                                try {
//                                    Node node = mgr.getNode(uname + "-broadcast");
//                                    node.subscribe(db.getUsername() + "@198.154.106.139");
//
//                                    L.debug("subscribed to " + uname);
//                                } catch (SmackException.NoResponseException e) {
//                                    L.error(e.getMessage());
//                                } catch (XMPPException.XMPPErrorException e) {
//                                    L.error(e.getMessage());
//                                } catch (SmackException.NotConnectedException e) {
//                                    L.error(e.getMessage());
//                                }
//
//
//                                db.close();

                        mNearbyUsers.add(geo);

                    } catch (JSONException e) {
                        L.error(""+e.getMessage());
                    }


                }

            }
        } else {
            //makeNotify("Error occurred while collecting users", AppMsg.STYLE_ALERT);
            L.makeText(GroupChatHomeActivity.this, "Error occurred while collecting users", AppMsg.STYLE_ALERT);
        }

//            }
//        }).start();

    }

    private LeafNode createCommentNode() throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {

        final SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
        db.openToRead();

        LeafNode node = null;

        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);


        ConfigureForm form = new ConfigureForm(DataForm.Type.form.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);

        node = (LeafNode) mgr.createNode(db.getUsername() + "-broadcast", form);

        db.close();
        return node;

    }

    private void downloadReceivedBroadcastsNearby() {

        List<Announcement> broadcasts = Announcement.downloadOffline(GroupChatHomeActivity.this);

        SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
        db.openToRead();
        if (broadcasts != null && !broadcasts.isEmpty() || mNearbyUsers != null && !mNearbyUsers.isEmpty()) {
            for (int i = 0; i < broadcasts.size(); i++) {
                Announcement ann = broadcasts.get(i);

                for (int j = 0; j < mNearbyUsers.size(); j++) {
                    Geolocation nearby = mNearbyUsers.get(j);
                    if (ann.getFrom().equals(nearby.getUsername()) || ann.getFrom().equals(db.getUsername())) {
                        addNewAnnouncement(ann);
                    }
                }
            }
        }
          //L.debug("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "+broadcasts.size());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });


        db.close();
    }

    private class OnBroadcastMessageTask implements OnExecutePendingTaskListener {

        private final String msgToBroadcast;
        private final Context context;

        public OnBroadcastMessageTask(Context context, final String msgToBroadcast) {
            this.msgToBroadcast = msgToBroadcast;
            this.context= context;
        }

        @Override
        public void onExecutePendingTask() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!XMPPService.xmpp.connection.isConnected()) {

                        XMPPManager.getInstance(context).instance = null;

                        XMPPService.xmpp = XMPPManager.getInstance(context);
                        XMPPService.xmpp.connect("onCreate");

                    } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                        XMPPService.xmpp.login();

                    } else {
                        final SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
                        db.openToRead();
                        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                        LeafNode node = null;

                        try {
                            node = mgr.getNode(db.getUsername() + "-broadcast");
                            //node.subscribe(db.getUsername() + "@198.154.106.139");

                        } catch (SmackException.NoResponseException e) {
                            L.error(e.getMessage());
                        } catch (XMPPException.XMPPErrorException e) {
                            L.error(e.getMessage());
                        } catch (SmackException.NotConnectedException e) {
                            L.error(e.getMessage());
                        }

                        if (node == null) {
                            try {
                                node = createCommentNode();
                            } catch (SmackException.NotConnectedException e) {
                                L.error(e.getMessage());
                            } catch (XMPPException.XMPPErrorException e) {
                                L.error(e.getMessage());
                            } catch (SmackException.NoResponseException e) {
                                L.error(e.getMessage());
                            }
                        }

                        if (node != null) {

                            //check if user is already subscribe
                            List<Subscription> subs = null;
                            try {
                                subs = node.getSubscriptions();
                            } catch (SmackException.NoResponseException e) {
                                L.error(e.getMessage());
                            } catch (XMPPException.XMPPErrorException e) {
                                L.error(e.getMessage());
                            } catch (SmackException.NotConnectedException e) {
                                L.error(e.getMessage());
                            }

                            boolean isAlreadySubscribe = false;
                            if (subs != null && !subs.isEmpty()) {
                                for (int i = 0; i < subs.size(); i++) {
                                    L.debug(subs.get(i).getJid() + ": " + subs.get(i).getId());
                                    //only subscribe if name don't appear in the list
                                    if (subs.get(i).getJid().split("@")[0].equals(db.getUsername())) {
                                        L.debug(db.getUsername() + " is already subscribed to " + node.getId());
                                        isAlreadySubscribe = true;
                                        break;
                                    }

                                }
                            } else {
                                L.error("sbscription is empty!!");
                                isAlreadySubscribe = false;
                            }

                            if (!isAlreadySubscribe) {
                                try {
                                    node.subscribe(db.getUsername() + "@198.154.106.139");
                                    isAlreadySubscribe = true;
                                    L.debug("subscribe to " + node.getId());
                                } catch (SmackException.NoResponseException e) {
                                    L.error(e.getMessage());
                                } catch (XMPPException.XMPPErrorException e) {
                                    L.error(e.getMessage());
                                } catch (SmackException.NotConnectedException e) {
                                    L.error(e.getMessage());
                                }
                            }


                            final String from, body, date, locLocal;
                            int reach = 0;
                            final long[] dateTime = new long[1];

                            dateTime[0] = System.currentTimeMillis();

                            from = db.getUsername();
                            body = msgToBroadcast;
                            date = Long.toString(dateTime[0]);

                            locLocal = (mAddress != null && !mAddress.isEmpty()) ? mAddress : "";

                            reach = mNearbyUsers.size();
                            Announcement ann = new Announcement(from, body, date, locLocal, reach, true);
                            Gson gson = new Gson();
                            try {

                                node.send(new PayloadItem(null,
                                        new SimplePayload("broadcast", "pubsub:nexpa:broadcast", "<broadcast xmlns='pubsub:nexpa:broadcast'>" + gson.toJson(ann) + "</broadcast>")));
                                L.debug("sending bradcast!!!");
                            } catch (SmackException.NoResponseException e) {
                                L.error(e.getMessage());
                            } catch (XMPPException.XMPPErrorException e) {
                                L.error(e.getMessage());
                            } catch (SmackException.NotConnectedException e) {
                                L.error(e.getMessage());
                            }

                            db.close();

                        }
                    }


                }
            }).start();

        }
    };

    String locationName = "";

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);

    }

    private XMPPService mService;
    private boolean mBound;

    @Override
    public void OnServiceConnected(XMPPService service) {
        mService = service;
        mBound = true;
    }

    @Override
    public void OnServiceDisconnected() {

        mService = null;
        mBound = false;

    }

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

        //L.debug("GroupChatHome, startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        mRequestingLocationUpdates = true;
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

            sendNewLocToServer(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            generateAddress(mCurrentLocation);
            //L.debug("latitude: " + String.valueOf(mCurrentLocation.getLatitude()));
            //L.debug("longitude: " + String.valueOf(mCurrentLocation.getLongitude()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadNearbyUsers();
                    subscribeToUsers();
                    //downloadReceivedBroadcastsNearby();
                }
            }).start();


        }

    }


    private void subscribeToUsers() {

        final SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
        db.openToRead();

        ExecutorService ex = Executors.newFixedThreadPool(5);

        for (final Geolocation nearby : mNearbyUsers) {
            ex.submit(new Runnable() {
                @Override
                public void run() {
                    PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);
                    try {
                        Node node = mgr.getNode(nearby.getUsername() + "-broadcast");
                        node.subscribe(db.getUsername() + "@198.154.106.139");

                        L.debug("subscribed to " + nearby.getUsername());
                    } catch (SmackException.NoResponseException e) {
                        L.error(e.getMessage());
                    } catch (XMPPException.XMPPErrorException e) {
                        L.error(e.getMessage());
                    } catch (SmackException.NotConnectedException e) {
                        L.error(e.getMessage());
                    }
                }
            });

        }
        ex.shutdown();
        try {
            ex.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            L.error(e.getMessage());
        }

        db.close();
    }

    private synchronized void generateAddress(final Location loc) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Geocoder geocoder = new Geocoder(GroupChatHomeActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                //L.debug("latitude: " + String.valueOf(loc.getLatitude()));
                // L.debug("longitude: " + String.valueOf(loc.getLongitude()));

                if (!Geocoder.isPresent()) {
                    L.makeText(GroupChatHomeActivity.this, getResources().getString(R.string.no_geocoder_available),
                            AppMsg.STYLE_ALERT);
                    mAddress = null;
                }

                try {
                    addresses = geocoder.getFromLocation(
                            mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude(),
                            // In this sample, get just a single address.
                            1);
                } catch (IOException e) {
                    L.error(e.getMessage());
                }

                String address_ = null;
                String errorMessage = "";
                if (addresses == null || addresses.size() == 0) {
                    if (errorMessage.isEmpty()) {
                        errorMessage = getString(R.string.no_address_found);
                        L.error(errorMessage);
                    }

                } else {
                    Address address = addresses.get(0);
                    //ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.
                    StringBuilder addressBuilder = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        //addressFragments.add(address.getAddressLine(i));
                        //L.debug("address: "+address.getAddressLine(i));
                        addressBuilder.append(address.getAddressLine(i));
                        if (i != address.getMaxAddressLineIndex() - 1)
                            addressBuilder.append(", ");
                    }

                    //L.debug(getString(R.string.address_found));
                    address_ = addressBuilder.toString();

                }

                mAddress = address_;

            }
        }).start();

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
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        if (mCurrentLocation != null) {
            //L.debug("new latitude: " + String.valueOf(mCurrentLocation.getLatitude()));
            //L.debug("new longitude: " + String.valueOf(mCurrentLocation.getLongitude()));
            generateAddress(mCurrentLocation);

            sendNewLocToServer(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
    }


    class ItemEventCoordinator implements ItemEventListener {

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            L.debug("Item count: " + items);


        }
    }

    public interface OnUpdateUIListener {
        public void onUpdateUI();
    }
}