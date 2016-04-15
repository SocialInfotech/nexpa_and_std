package com.lpoezy.nexpa.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.MAMExtensionProvider;
import com.lpoezy.nexpa.objects.MAMFinExtensionProvider;
import com.lpoezy.nexpa.objects.MessageArchiveWithIQ;
import com.lpoezy.nexpa.objects.MessageResultElement;
import com.lpoezy.nexpa.objects.Messages;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatActivity extends Activity implements Correspondent.OnCorrespondentUpdateListener, XMPPManager.OnConnectedToOPenfireListener {
    private com.lpoezy.nexpa.chatservice.ChatAdapterActivity adapter;

    public boolean isMine;
    private XMPPConnection connection;
    private Handler mHandler = new Handler();

    private EditText recipient;
    private EditText textMessage;
    // private ListView listview;
    private RecyclerView listview;
    String int_mes;
    String int_broad;
    String int_b_date;
    Intent intentMes;

    private boolean mFrmRotation;

    private Correspondent mCorrespondent;

    private ChatAdapter mAdapter;


    private Messages mComments;

    private long mCorrespondentId;

    private SwipyRefreshLayout mSwipeRefreshLayout;

    private LinearLayout textMessageContainer;

    private Button send;

    private String mCorrespondentName;

    public static boolean isRunning = false;
    private Random random;
    private List<ChatMessage> mChatMsgs;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutmain);

        if (savedInstanceState != null) {
            mFrmRotation = true;
        } else {
            mFrmRotation = false;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e("NOTICE", "Device cannot handle ActionBar");
        }

        // setConnection();
        recipient = (EditText) this.findViewById(R.id.toET);
        textMessage = (EditText) this.findViewById(R.id.chatET);
        textMessageContainer = (LinearLayout) this.findViewById(R.id.chatETContainer);
        // listview = (ListView) this.findViewById(R.id.listMessages);
        listview = (RecyclerView) this.findViewById(R.id.listMessages);
        final LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);

        lm.setStackFromEnd(true);
        listview.setLayoutManager(lm);

        // listview.addItemDecoration(new
        // DividerItemDecoration(ChatActivity.this,
        // DividerItemDecoration.VERTICAL_LIST));

        intentMes = getIntent();
        // String email = intentMes.getStringExtra("email");
//        final long userId = getIntent().getLongExtra("userid", -1);
//        mCorrespondentId = userId;
        mCorrespondentName = intentMes.getStringExtra("username");
        //final String username = intentMes.getStringExtra("username");
        final String email = getIntent().getStringExtra("email");
        final String fname = getIntent().getStringExtra("fname");


        mComments = new Messages();
        mChatMsgs = new ArrayList<ChatMessage>();
        mAdapter = new ChatAdapter(ChatActivity.this);

        listview.setAdapter(mAdapter);

        int_mes = "";
        int_broad = "";
        int_b_date = "";

        try {
            int_mes = intentMes.getStringExtra("INTENT_MESSAGE");
            int_broad = intentMes.getStringExtra("INTENT_MESSAGE_TYPE");
            int_b_date = intentMes.getStringExtra("INTENT_MESSAGE_DATE");
        } catch (Exception e) {
        }

        // intentMes.putExtra("email", email);
        // intentMes.putExtra("username", username);
        // intentMes.putExtra("fname", fname);

        // String fname = intentMes.getStringExtra("fname");

        recipient.setText(mCorrespondentName + "@vps.gigapros.com/Smack", TextView.BufferType.EDITABLE);
        recipient.setVisibility(View.INVISIBLE);
        // Smack
        if (int_broad != null && int_broad.equals("BROADCAST")) {
            int_broad = "";
            textMessage.setText("In reply to: '" + int_mes + "',\nPosted " + int_b_date + "\n\n");
            textMessage.setSelection(textMessage.getText().length());
        } else {
            textMessage.setText("");
        }
        random = new Random();
        // Set a listener to send a chat text message
        send = (Button) this.findViewById(R.id.sendBtn);
        send.setOnClickListener(new View.OnClickListener() {
            private boolean isReconnecting;

            public void onClick(View view) {
                // String to = username + "@vps.gigapros.com/Smack";
                final String to = recipient.getText().toString();
                final String message = textMessage.getText().toString();


                if (!message.equalsIgnoreCase("")) {

                    SQLiteHandler db = new SQLiteHandler(ChatActivity.this);
                    db.openToRead();

                    final ChatMessage chatMessage = new ChatMessage(db.getUsername(), mCorrespondentName,
                            message, "" + random.nextInt(1000), true);

                    chatMessage.setMsgID();
                    chatMessage.body = message;
                    //chatMessage.Date = CommonMethods.getCurrentDate();
                    //chatMessage.Time = CommonMethods.getCurrentTime();

                    textMessage.setText("");


                    if (mBounded) {
                        mService.sendMessage(chatMessage);

                        mChatMsgs.add(chatMessage);
                        mAdapter.notifyDataSetChanged();
                        listview.smoothScrollToPosition(mChatMsgs.size() - 1);
                        //lat, 14.5580795
                        //long, 121.0196150
                    }

                    db.close();

                }
            }
        });


        mSwipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.niagara, R.color.buttercup, R.color.niagara);
        mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.carrara));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {

                if (mBounded) {
                    retriveChatMessages();

                }
            }
        });


        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mComments.markMyOfflineMsgsAsRead(ChatActivity.this, mCorrespondentName);
    }

    private boolean mBounded;
    private XMPPService mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mService = null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            L.debug("ChatACtivity, onServiceConnected");
            mService = ((LocalBinder<XMPPService>) service).getService();

            retriveChatMessages();
        }
    };

    private void dissmissSwipeToRefresh() {

        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }

    private class OnRetrieveChatMessagesWith implements OnExecutePendingTaskListener {
        private final String with;

        public OnRetrieveChatMessagesWith(String with) {
            this.with = with;
        }

        @Override
        public void onExecutePendingTask() {
            L.debug("OnRetrieveChatMessagesWith: "+this.with);

            if (!XMPPService.xmpp.connection.isConnected()) {

                dissmissSwipeToRefresh();

                XMPPManager.getInstance(ChatActivity.this).instance = null;

                XMPPService.xmpp = XMPPManager.getInstance(ChatActivity.this);

                XMPPService.xmpp.connect("onCreate");

            } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                dissmissSwipeToRefresh();

                XMPPService.xmpp.login();
            } else {

                XMPPService.xmpp.addOnProcessMessageListener(new OnProcessMessage(){
                    @Override
                    public void onProcessMessage(final ChatMessage chatMessage) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(!chatMessage.senderName.equals(mCorrespondentName))return;
                                //NewMessage comment = new NewMessage(senderName, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);
                                mChatMsgs.add(chatMessage);
                                //mComments.add(comment);
                                mAdapter.notifyDataSetChanged();
                                listview.smoothScrollToPosition(mChatMsgs.size() - 1);


                            }
                        });
                    }
                });

                MessageArchiveWithIQ mam = new MessageArchiveWithIQ(this.with);
                mam.setType(IQ.Type.set);
                try {
                    XMPPService.xmpp.connection.sendStanza(mam);
                } catch (SmackException.NotConnectedException e) {
                    L.error("retrieveListOfCollectionsFrmMsgArchive: " + e.getMessage());

                }

                final List<MessageResultElement> msgElements = new ArrayList<MessageResultElement>();

                ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0",
                        new MAMExtensionProvider(
                                new MessageResultElement.OnParseCompleteListener() {

                                    @Override
                                    public void onParseComplete(MessageResultElement msg) {

                                        //L.debug("msgs: "+msgElements.size());

                                        msgElements.add(msg);
                                    }
                                }
                        ));

                ProviderManager.addExtensionProvider("fin", "urn:xmpp:mam:0",
                        new MAMFinExtensionProvider(
                                new MAMFinExtensionProvider.OnParseCompleteListener() {

                                    @Override
                                    public void onParseComplete(final int first, final int last, final int count) {

                                       // L.debug("msgs: " + msgElements.size() + ", onParseComplete: first: " + first + ", last: " + last + ", count: " + count);
                                        mSwipeRefreshLayout.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSwipeRefreshLayout.setRefreshing(false);
                                            }
                                        }, 500);


                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                SQLiteHandler db = new SQLiteHandler(ChatActivity.this);
                                                db.openToRead();
                                                final UserProfile uProfile = new UserProfile();
                                                uProfile.setUsername(db.getUsername());

                                                uProfile.loadVCard(XMPPService.xmpp.connection);

                                                // L.debug("updateGrid, uname: " + uProfile.getUsername() + ", desc: " + uProfile.getDescription() + ", " + uProfile.getAvatarImg());
                                                L.debug(uProfile.getUsername()+", "+uProfile.getAvatarImg());
                                                if (uProfile.getAvatarImg() != null) {
                                                    mUserAvatar = uProfile.getAvatarImg();

                                                    resetAdapter();
                                                }
                                                db.close();

                                            }
                                        }).start();


                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                final UserProfile cProfile = new UserProfile();
                                                cProfile.setUsername(mCorrespondentName);

                                                cProfile.loadVCard(XMPPService.xmpp.connection);

                                                // L.debug("updateGrid, uname: " + uProfile.getUsername() + ", desc: " + uProfile.getDescription() + ", " + uProfile.getAvatarImg());

                                                if (cProfile.getAvatarImg() != null) {
                                                    mCorrespondentAvatar = cProfile.getAvatarImg();

                                                    resetAdapter();
                                                }

                                            }
                                        }).start();


                                        L.debug("ChatActivity, onParseComplete");
                                        mChatMsgs.clear();


                                        Gson gson = new Gson();

                                        for (MessageResultElement msg : msgElements) {

                                            L.debug("to: " + msg.getTo() + ", body: " + msg.getBody() + ", from: " + msg.getFrom());
                                            ChatMessage chat = gson.fromJson(msg.getBody(), ChatMessage.class);
                                            mChatMsgs.add(chat);
                                        }

                                        resetAdapter();

                                    }
                                }
                        ));
            }

        }
    }

    ;

    private void retriveChatMessages() {

        String with = getIntent().getStringExtra("with");

        if (with != null && !with.isEmpty()) {
            mCorrespondentName = with.split("@")[0];
        }

        final String newWith = mCorrespondentName + "@198.154.106.139";



        //mService.retrieveListOfCollectionsFrmMsgArchive(newWith);
        mService.onExecutePendingTask(new OnRetrieveChatMessagesWith(newWith));


    }

    @Override
    protected void onResume() {

        super.onResume();

        isRunning = true;

        //registerReceiver(mReceivedMessage, new IntentFilter(AppConfig.ACTION_RECEIVED_MSG));

        Intent service = new Intent(this, XMPPService.class);
        bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        L.debug("ChatActivity, onPause... ");
        // connection.disconnect();
        // Log.e("NULLIFIED","");

        isRunning = false;

        //unregisterReceiver(mReceivedMessage);

        if (mService != null) {


            unbindService(mServiceConn);
        }

        if (isFinishing()) {


            // will make sure that the othe activities/fragments,
            // that are dependent on message count will automatically,
            // update themselves
            L.debug("ChatActivity, update msg count in tab... ");
            Intent broadcast = new Intent(AppConfig.ACTION_RECEIVED_MSG);
            sendBroadcast(broadcast);
        }
    }



    private Bitmap mCorrespondentAvatar;
    private Bitmap mUserAvatar;

    private void resetAdapter() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onConnectedToOpenfire(XMPPConnection connection) {


    }


    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout wrapper;
            TextView countryName;
            CircularImageView iv;

            public ViewHolder(View itemView) {
                super(itemView);
                wrapper = (RelativeLayout) itemView.findViewById(R.id.wrapper);
                // countryName = (TextView) itemView.findViewById(R.id.comment);

                iv = new CircularImageView(context);
                iv.setId(R.id.img_1);
                RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(75, 75);
                wrapper.addView(iv, layoutParams0);

                countryName = new TextView(context);
                countryName.setId(R.id.tv_2);

                int width = 250;
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                switch (metrics.densityDpi) {
                    case DisplayMetrics.DENSITY_LOW:
                        //L.debug("low density");
                        width = (int) (width * 0.25);
                        break;
                    case DisplayMetrics.DENSITY_MEDIUM:
                        //L.debug("medium density");
                        width = (int) (width * 0.5);
                        break;
                    case DisplayMetrics.DENSITY_HIGH:
                        //L.debug("high density");
                        width = width;
                        break;
                    default:
                        //L.debug("x high density");
                        width = width * 2;
                        break;
                }


                RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
                wrapper.addView(countryName, layoutParams1);

            }

        }

        private LayoutInflater inflater;
        private Context context;

        public ChatAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public int getItemCount() {

            return mChatMsgs.size();
        }


        @Override
        public void onBindViewHolder(ViewHolder vh, int pos) {

            try {

                ChatMessage comment = mChatMsgs.get(pos);
                vh.countryName.setText(comment.body);

                SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                db.openToRead();
                boolean isMine = comment.senderName.equals(db.getUsername()) ? true : false;
                db.close();

                vh.countryName.setBackgroundResource(R.drawable.bubble_green);
//			if (comment.isSuccessful()) {
                vh.countryName.setBackgroundResource(isMine ? R.drawable.bubble_green : R.drawable.bubble_yellow);
//			} else {
//				vh.countryName.setBackgroundResource(!comment.isLeft() ? R.drawable.bubble_failed : R.drawable.bubble_yellow);
//			}
                Bitmap rawImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_sample_girl);
                if (isMine) {
                    if (mUserAvatar != null) {
                        rawImage = mUserAvatar;
                    }

                    vh.iv.setImageBitmap(rawImage);
                    // will remove previously added rule to this view
                    ((RelativeLayout.LayoutParams) vh.countryName.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF, 0);
                    // add new rule for the layout to implement
                    ((RelativeLayout.LayoutParams) vh.iv.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF,
                            vh.countryName.getId());

                } else {

                    if (mCorrespondentAvatar != null) {
                        rawImage = mCorrespondentAvatar;
                    }

                    vh.iv.setImageBitmap(rawImage);

                    // will remove previously added rule to this view
                    ((RelativeLayout.LayoutParams) vh.iv.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF, 0);
                    // add new rule for the layout to implement
                    ((RelativeLayout.LayoutParams) vh.countryName.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF,
                            vh.iv.getId());


                }

                vh.wrapper.setGravity(isMine ? Gravity.RIGHT : Gravity.LEFT);

            } catch (IndexOutOfBoundsException e) {
                L.error(e.getMessage());
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup root, int arg1) {

            View itemView = inflater.inflate(R.layout.listitem, root, false);
            return new ViewHolder(itemView);
        }


    }

    @Override
    public void onCorrespondentUpdate() {
        listview.post(new Runnable() {

            @Override
            public void run() {

                mAdapter.notifyDataSetChanged();

            }
        });

    }

    public interface OnProcessMessage {
        public void onProcessMessage(ChatMessage chatMessage);
    }

//    public interface OnRetrieveMessageArchiveListener {
//        public void onRetrieveMessageArchive(List<ChatMessage> conversation);
//    }

}