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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.MessageElement;
import com.lpoezy.nexpa.objects.Messages;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.RoundedImageView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatActivity extends Activity implements Correspondent.OnCorrespondentUpdateListener, XMPPService.OnProcessMessage, MessageElement.OnParseCompleteListener {
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
    private List<ChatMessage> chatMsgs;

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
        final long userId = getIntent().getLongExtra("userid", -1);
        mCorrespondentId = userId;
        mCorrespondentName = intentMes.getStringExtra("username");
        //final String username = intentMes.getStringExtra("username");
        final String email = getIntent().getStringExtra("email");
        final String fname = getIntent().getStringExtra("fname");


        mComments = new Messages();
        chatMsgs = new ArrayList<ChatMessage>();
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

                        chatMsgs.add(chatMessage);
                        mAdapter.notifyDataSetChanged();
                        listview.smoothScrollToPosition(chatMsgs.size() - 1);
                        //lat, 14.5580795
                        //long, 121.0196150
                    }

                    db.close();
//					chatAdapter.add(chatMessage);
//					chatAdapter.notifyDataSetChanged();
//					MainActivity activity = ((MainActivity) getActivity());
//					activity.getmService().xmpp.sendMessage(chatMessage);
                }

				/*/
                if(!text.isEmpty()){
					
					textMessage.setText("");
					
					final Message msg = new Message(to, Message.Type.chat);
					msg.setBody(text);

					connection = XMPPLogic.getInstance().getConnection();
					
					String senderName = "";
					String receiverName = mCorrespondentName;
					String body = text;
					boolean isLeft = false;
					boolean isSuccessful = false;
					boolean isUnread = true;
					boolean isSyncedOnline = false;
					long date = System.currentTimeMillis();
					
					Correspondent correspondent = new Correspondent(receiverName);
					correspondent.saveOffline(ChatActivity.this);
					
					if ((connection == null) || (!connection.isConnected())) {
						
						L.debug("XMPPChatDemoActivity, reconnecting...");

						isSuccessful = false;
						
						NewMessage comment = new NewMessage(
						, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);
						comment.saveMySentMsgOffline(ChatActivity.this);
						//long now = System.currentTimeMillis();
						//String date = DateUtils.millisToSimpleDate(now, DateFormatz.DATE_FORMAT_5);
						//comment.date = date;
						
						mComments.add(comment);
						adapter.notifyDataSetChanged();
						listview.smoothScrollToPosition(mComments.size() - 1);
						// adapter.add(comment);
						// listview.setAdapter(adapter);
						//mCorrespondent.addMessage(comment);

						if (!isReconnecting) {
							isReconnecting = true;
							Account ac = new Account();
							SQLiteHandler db = new SQLiteHandler(getApplicationContext());
							db.openToWrite();

							ac.LogInChatAccount(db.getUsername(), db.getEncryptedPassword(), db.getEmail(),
									new OnXMPPConnectedListener() {

								@Override
								public void onXMPPConnected(XMPPConnection connection) {

									isReconnecting = false;

								}
							});

							db.close();
						}

					}

					else {
						L.debug("XMPPChatDemoActivity, Sending text " + text + " to " + to);
						//connection.sendPacket(msg);
					
						isSuccessful = true;
						NewMessage comment = new NewMessage(senderName, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);
						comment.saveMySentMsgOffline(ChatActivity.this);
						
						mComments.add(comment);
						adapter.notifyDataSetChanged();
						listview.smoothScrollToPosition(mComments.size() - 1);
						
					}
					
				}
				//*/
            }
        });

        //updateList();

    }

    protected void updateList() {

        final long userId = getIntent().getLongExtra("userid", -1);
        final String username = getIntent().getStringExtra("username");

        mCorrespondent = new Correspondent(username);
        mCorrespondent.addListener(this);
        mComments.downloadMyOfflineConversationWith(ChatActivity.this, mCorrespondentName);

        //mCorrespondent.downloadOfflineMessagesByIds(ChatActivity.this, Long.parseLong(db.getLoggedInID()), userId);

        new Thread(new Runnable() {

            @Override
            public void run() {

                mCorrespondent.downloadProfilePicOnline(ChatActivity.this, userId);

            }
        }).start();

    }

    // receiving chtMessages will be handle by receivedMessage
    // in ChatMessagesService

    /*/
    private BroadcastReceiver mReceivedMessage = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            mHandler.post(new Runnable() {
                public void run() {

                    String senderName = intent.getStringExtra("username");
                    String receiverName = "";
                    String body = intent.getStringExtra("msg");
                    boolean isLeft = true;
                    boolean isSuccessful = true;
                    boolean isUnread = false;
                    boolean isSyncedOnline = false;
                    long date = intentMes.getLongExtra("date", 1L);
                    //don't reveal msg,
                    //if the sender is not the current correspondent,
                    //of the user
                    L.debug("senderName: "+senderName+", mCorrespondentName: "+mCorrespondentName);
                    if(!senderName.equals(mCorrespondentName))return;


                    // mCorrespondentId = userId;
                    //mCorrespondent.setId(userId);
                    L.debug("msg received from ..." + senderName);
                    // final String username =
                    // intentMes.getStringExtra("username");


                    //NewMessage comment = new NewMessage(senderName, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);

                    mComments.add(comment);
                    adapter.notifyDataSetChanged();
                    listview.smoothScrollToPosition(mComments.size() - 1);

                }
            });
        }
    };
//*/
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

            mService = ((LocalBinder<XMPPService>) service).getService();
            mService.addMessageListener(ChatActivity.this);

            String with = getIntent().getStringExtra("with");

            if (with != null && !with.isEmpty()) {
                mCorrespondentName = with.split("@")[0];
            }


            final String newWith = mCorrespondentName + "@198.154.106.139";
            mService.retrieveListOfCollectionsFrmMsgArchive(newWith);

            mService.addMAMObserver(ChatActivity.this);

        }
    };

    public void onParseComplete(final List<MessageElement> msgs, final int first, final int last, final int count) {

        chatMsgs.clear();

        Gson gson = new Gson();

        for(MessageElement msg : msgs){

            //L.debug("to: "+msg.getTo()+", body: "+msg.getBody()+", from: "+msg.getFrom());
            ChatMessage chat = gson.fromJson(msg.getBody(), ChatMessage.class);
            chatMsgs.add(chat);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
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

            mService.removeMAMObserver(ChatActivity.this);

            unbindService(mServiceConn);
        }

        // saveVCard chtMessages here,
        // and clear all the conversation array
        // L.debug("ChatActivity, saving msgs... ");

        // mCorrespondent.saveOffline(ChatActivity.this, true);
        // mCorrespondent.saveVCard(ChatActivity.this, true);

        if (isFinishing()) {

//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					
//					mCorrespondent.saveOnline(ChatActivity.this, true);
//					
//				}
//			}).start();


            // will make sure that the othe activities/fragments,
            // that are dependent on message count will automatically,
            // update themselves
            L.debug("ChatActivity, update msg count in tab... ");
            Intent broadcast = new Intent(AppConfig.ACTION_RECEIVED_MSG);
            sendBroadcast(broadcast);
        }
    }

    @Override
    public void onProcessMessage(final ChatMessage chatMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {


                //if(!chatMessage.sender.equals(mCorrespondentName))return;


                // mCorrespondentId = userId;
                //mCorrespondent.setId(userId);
                //L.debug("msg received from ..." + senderName);
                // final String username =
                // intentMes.getStringExtra("username");


                //NewMessage comment = new NewMessage(senderName, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);
                chatMsgs.add(chatMessage);
                //mComments.add(comment);
                mAdapter.notifyDataSetChanged();
                listview.smoothScrollToPosition(chatMsgs.size() - 1);
            }
        });
    }


    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout wrapper;
            TextView countryName;
            ImageView iv;

            public ViewHolder(View itemView) {
                super(itemView);
                wrapper = (RelativeLayout) itemView.findViewById(R.id.wrapper);
                // countryName = (TextView) itemView.findViewById(R.id.comment);

                iv = new ImageView(context);
                iv.setId(R.id.img_1);
                RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(50, 50);
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

            return chatMsgs.size();
        }

        //<iq type="result" id="qvtHC-14" to="momo@198.154.106.139/Smack">
        // <chat xmlns="urn:xmpp:archive" with="leki@198.154.106.139" start="2016-03-23T00:03:36.282Z">
        // <from secs="0">
        // <body>{"body":"ghbnn","senderName":"leki","msgid":"980-27","receiver":"momo","sender":"leki","isMine":true}</body></from>
        // <from secs="4"><body>{"body":"fghjjj","senderName":"leki","msgid":"784-75","receiver":"momo","sender":"leki","isMine":true}</body></from><set xmlns="http://jabber.org/protocol/rsm"><first index="0">0</first><last>1</last><count>2</count></set></chat></iq>

        @Override
        public void onBindViewHolder(ViewHolder vh, int pos) {

            ChatMessage comment = chatMsgs.get(pos);
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

            if (isMine) {

                Bitmap bmp = getUserPic(vh.iv);
                vh.iv.setImageBitmap(bmp);
                // will remove previously added rule to this view
                ((RelativeLayout.LayoutParams) vh.countryName.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF, 0);
                // add new rule for the layout to implement
                ((RelativeLayout.LayoutParams) vh.iv.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF,
                        vh.countryName.getId());

            } else {


                Bitmap bmp = getCorrespondentPic();
                vh.iv.setImageBitmap(bmp);

                // will remove previously added rule to this view
                ((RelativeLayout.LayoutParams) vh.iv.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF, 0);
                // add new rule for the layout to implement
                ((RelativeLayout.LayoutParams) vh.countryName.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF,
                        vh.iv.getId());


            }

            vh.wrapper.setGravity(isMine ? Gravity.RIGHT : Gravity.LEFT);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup root, int arg1) {

            View itemView = inflater.inflate(R.layout.listitem, root, false);
            return new ViewHolder(itemView);
        }

        private Bitmap getCorrespondentPic() {

            Bitmap rawImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_sample_girl);

//			if (mCorrespondent.getProfilePic() != null) {
//				rawImage = mCorrespondent.getProfilePic();
//			}

            RoundedImageView riv = new RoundedImageView(context);
            Bitmap circImage = riv.getCroppedBitmap(rawImage, 80);

            return circImage;
        }

        private Bitmap userPic;

        private Bitmap getUserPic(ImageView imgRight) {

            String imgDecodableString = ProfilePicture.getUserImgDecodableString(context);

            Bitmap rawImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_sample_girl);
            if (userPic == null) {
                if (imgDecodableString != null && !imgDecodableString.isEmpty()) {

                    // Get the dimensions of the View
                    int targetW = imgRight.getWidth();
                    int targetH = imgRight.getHeight();

                    BmpFactory bmpFactory = new BmpFactory();
                    userPic = rawImage = bmpFactory.getBmpWithTargetWTargetHFrm(targetW, targetH, imgDecodableString);

                }

            } else {
                rawImage = userPic;
            }

            RoundedImageView riv = new RoundedImageView(context);
            Bitmap circImage = riv.getCroppedBitmap(rawImage, 80);

            return circImage;
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

    public interface OnRetrieveMessageArchiveListener {
        public void onRetrieveMessageArchive(List<ChatMessage> conversation);
    }

}