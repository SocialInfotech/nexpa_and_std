package com.lpoezy.nexpa.activities;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.Announcement;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.time.packet.Time;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.Locale;

public class GroupChatHomeActivity extends AppCompatActivity implements XMPPService.OnServiceConnectedListener {

    Button btnStartChat;
    Button btnCancel;
    Button dialogButtonOK;
    EditText edBroad;
    String mNickName;
    SQLiteHandler db;
    Message msg;
    String strUser;
    int repeater;
    int interactor;
    LinearLayout lnBroadcast;
    LinearLayout lnBroadcastMini;
    LinearLayout lnEmpty;
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
    SimpleCursorAdapter mAdapter;
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


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("WINDOW", "PAUSED ");
    }

    @Override
    protected void onResume() {
        super.onResume();
//*/
        new Thread(new Runnable() {
            @Override
            public void run() {

                SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
                db.openToRead();
                // Create a pubsub manager using an existing XMPPConnection
                PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                LeafNode node = null;
                try {

                    //node = mgr.getNode(db.getUsername()+"-broadcast");

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

                try {
                    node = mgr.getNode(db.getUsername() + "-broadcast");

                    node.addItemEventListener(new ItemEventCoordinator());

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
        }).start();
//*/


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_home);

        lnBroadcast = (LinearLayout) findViewById(R.id.lnBroadcast);
        lnEmpty = (LinearLayout) findViewById(R.id.lnBroadcastEmpty);
        lnBroadcastExist = (LinearLayout) findViewById(R.id.lnBroadcastExist);
        //lnBroadcastExist.setOnClickListener(mBuyButtonClickListener);

        lnBroadcastMini = (LinearLayout) findViewById(R.id.lnBroadcastMini);
        dialogBroadcast = new Dialog(GroupChatHomeActivity.this);
        dialogBroadcast.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBroadcast.setContentView(R.layout.activity_group_chat_main);
        edBroad = (EditText) dialogBroadcast.findViewById(R.id.txtBroadcast);
        btnCancel = (Button) dialogBroadcast.findViewById(R.id.btnClose);
        //btnRefresher = (Button) findViewById(R.id.btnOptions);
        //btnOptions = (Button) findViewById(R.id.btnOptions);
        txtConnection = (TextView) findViewById(R.id.txt_broad_stat);


        mListView = (ListView) findViewById(R.id.listview);

        animFade = AnimationUtils.loadAnimation(GroupChatHomeActivity.this, R.anim.anim_fade_in_r);


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

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";



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

    private void openBroadcastDialog() {
        edBroad.setText("");
        dialogBroadcast.show();
        btnStartChat = (Button) dialogBroadcast.findViewById(R.id.btnStartLocChat);
        btnStartChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                final String msgToBroadcast = edBroad.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean msgSent = broadcastMessage(msgToBroadcast);

                        if (msgSent) {

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    dialogBroadcast.dismiss();

                                }
                            });
                        }
                    }
                }).start();


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

    private boolean broadcastMessage(final String msgToBroadcast) {
        SQLiteHandler db = new SQLiteHandler(GroupChatHomeActivity.this);
        db.openToRead();
        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

        LeafNode node = null;
        try {
            node = mgr.getNode(db.getUsername() + "-broadcast");

            final String from, body, date, locLocal;
            int reach = 0;
            final long[] dateTime = new long[1];
            Time time = new Time();
            try {
                XMPPService.xmpp.connection.sendStanza(time);

                XMPPService.xmpp.connection.addSyncStanzaListener(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {

                        dateTime[0] = ((Time)stanza).getTime().getTime();
                    }
                }, new StanzaFilter() {
                    @Override
                    public boolean accept(Stanza stanza) {

                        if(stanza.toString().contains("urn:xmpp:time"))
                            return true;
                        return false;
                    }
                });

                L.debug("time: " + time.getTime());
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            from = db.getUsername();
            body = msgToBroadcast;
            date = Long.toString(dateTime[0]);
            locLocal = "";
            reach = 1;


            Announcement ann = new Announcement(from, body, date, locLocal, reach);

//            ConfigureForm configureForm = new ConfigureForm(DataForm.Type.submit);
//            configureForm.setAccessModel(AccessModel.open);
//            configureForm.setDeliverPayloads(false);
//            configureForm.setNotifyRetract(true);
//            configureForm.setPersistentItems(true);
//            configureForm.setPublishModel(PublishModel.open);
//
//            node.sendConfigurationForm(configureForm);

            // Publish an Item with payload
            node.send(new PayloadItem(null,
                    new SimplePayload("broadcast", "pubsub:nexpa:broadcast", "<broadcast xmlns='pubsub:nexpa:broadcast'>" + msgToBroadcast + "</broadcast>")));

            return true;

        } catch (SmackException.NoResponseException e) {
            L.error(e.getMessage());
        } catch (XMPPException.XMPPErrorException e) {
            L.error(e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            L.error(e.getMessage());
        }
        db.close();
        return false;
    }

    String locationName = "";

    @Override
    public void OnServiceConnected(XMPPService service) {

    }

    @Override
    public void OnServiceDisconnected() {

    }


    class ItemEventCoordinator implements ItemEventListener {

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            L.debug("Item count: " + items);


        }
    }
}