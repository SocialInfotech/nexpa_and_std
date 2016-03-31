package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.Correspondents;
import com.lpoezy.nexpa.objects.MAMExtensionProvider;
import com.lpoezy.nexpa.objects.MAMFinExtensionProvider;
import com.lpoezy.nexpa.objects.MessageArchiveWithIQ;
import com.lpoezy.nexpa.objects.MessageResultElement;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.DividerItemDecoration;
import com.lpoezy.nexpa.utility.L;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHistoryListFragment extends Fragment implements Correspondent.OnCorrespondentUpdateListener {

    private OnShowChatHistoryListener mCallback;
    // private List<Correspondent> mBuddys;
    private Correspondents mBuddys;
    public ChatHistoryAdapter adapter;
    //public List<ListOfCollectionsIQ.Chat> collections = new ArrayList<ListOfCollectionsIQ.Chat>();

    // private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rvChatHistory;

    private List<LatestMessage> mLatestMsgs;

    public List<LatestMessage> getmLatestMsgs() {
        return mLatestMsgs;
    }

    private class LatestMessage {
        public String stamp;
        public Bitmap avatar;
        public ChatMessage chat;

        public LatestMessage() {
        }
    }


    public static ChatHistoryListFragment newInstance() {
        ChatHistoryListFragment fragment = new ChatHistoryListFragment();
        return fragment;
    }

    public ChatHistoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        try {
            mCallback = (OnShowChatHistoryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.getClass().getSimpleName() + " must implement OnShowChatHistoryListener interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_history_list, container, false);

        rvChatHistory = (RecyclerView) v.findViewById(R.id.rv_chat_history);
        rvChatHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvChatHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        // mBuddys = new ArrayList<Correspondent>();
        mLatestMsgs = new ArrayList<LatestMessage>();

        mBuddys = new Correspondents();

        mSwipeRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.niagara, R.color.buttercup, R.color.niagara);
        mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.carrara));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                onSwipeToRefresh();


            }
        });

        onSwipeToRefresh();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        adapter = new ChatHistoryAdapter(getActivity(), mCallback);
        rvChatHistory.setAdapter(adapter);

        return v;
    }

    private void onSwipeToRefresh() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (!XMPPService.xmpp.connection.isConnected()) {
                    //XMPPService.xmpp.connect("onCreate");


                } else if(!XMPPService.xmpp.connection.isAuthenticated()){
                    XMPPService.xmpp.login();
                }else{
                    retrivedMessageArchive();
                }

                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        }).start();

    }

    private void retrivedMessageArchive() {


        AbstractXMPPConnection connection = XMPPService.xmpp.connection;


        MessageArchiveWithIQ mam = new MessageArchiveWithIQ(null);
        mam.setType(IQ.Type.set);
        try {
            connection.sendStanza(mam);
        } catch (SmackException.NotConnectedException e) {
            L.error("retrieveListOfCollectionsFrmMsgArchive: " + e.getMessage());

        }

        final List<MessageResultElement> msgElements = new ArrayList<MessageResultElement>();

        ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0",
                new MAMExtensionProvider(
                        new MessageResultElement.OnParseCompleteListener() {

                            @Override
                            public void onParseComplete(MessageResultElement msg) {

                                //L.debug("msgs: "+msgs.size());

                                msgElements.add(msg);
                            }
                        }
                ));

        ProviderManager.addExtensionProvider("fin", "urn:xmpp:mam:0",
                new MAMFinExtensionProvider(
                        new MAMFinExtensionProvider.OnParseCompleteListener() {

                            @Override
                            public void onParseComplete(final int first, final int last, final int count) {

                                if (msgElements != null && !msgElements.isEmpty()) {
                                    L.debug("ChatHistoryActivity, onParseComplete");

                                    SQLiteHandler db = new SQLiteHandler(getActivity());
                                    db.openToWrite();

                                    //save messages to offline db
                                    db.deleteMsgArchive();
                                    db.saveMsgArchive(msgElements);

                                    db.close();
                                }

                                updateUI();

                                mSwipeRefreshLayout.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }, 500);

                            }
                        }
                ));


    }


    // receiving chtMessages will be handle by receivedMessage
    // in ChatMessagesService
    private BroadcastReceiver mReceivedMessage = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            L.debug("=============ChatHistoryList, message received================");

        }

    };

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        getActivity().unregisterReceiver(mReceivedMessage);
        // getActivity().unregisterReceiver(mReceivedCorrespondentUpdate);


    }


    @Override
    public void onResume() {
        L.debug("ChatHistory, onResume");
        super.onResume();

        getActivity().registerReceiver(mReceivedMessage, new IntentFilter(AppConfig.ACTION_RECEIVED_MSG));

        // getActivity().registerReceiver(mReceivedCorrespondentUpdate, new
        // IntentFilter(Correspondent.ACTION_UPDATE));
        // int count = OneComment.getUnReadMsgCountOffline(getActivity());
        // L.debug("count: "+count+", mBuddys.isEmpty: "+mBuddys.isEmpty());


    }

    public void updateUI() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {

        //get collections from offline db
        final SQLiteHandler db = new SQLiteHandler(getActivity());
        db.openToRead();
        List<MessageResultElement> msgs = db.downloadMsgArchive();
        Gson gson = new Gson();
        if (msgs != null && !msgs.isEmpty()) {

            mLatestMsgs.clear();

            for (MessageResultElement msg : msgs) {

                final LatestMessage lMsg = new LatestMessage();
                lMsg.stamp = msg.getStamp();
                lMsg.chat = gson.fromJson(
                        msg.getBody(), ChatMessage.class);


                if (XMPPService.xmpp.loggedin) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            String sendername = db.getUsername();
                            String uname = sendername.equals(lMsg.chat.sender) ? lMsg.chat.receiver : lMsg.chat.sender;

                            final UserProfile uProfile = new UserProfile();
                            uProfile.setUsername(uname);

                            uProfile.loadVCard(XMPPService.xmpp.connection);

                            // L.debug("updateGrid, uname: " + uProfile.getUsername() + ", desc: " + uProfile.getDescription() + ", " + uProfile.getAvatarImg());

                            if (uProfile.getAvatarImg() != null) {
                                lMsg.avatar = uProfile.getAvatarImg();

                                resetAdapter();
                            }

                        }
                    }).start();
                }


                mLatestMsgs.add(lMsg);


            }
        }

        db.close();
        resetAdapter();

//            }
//        }).start();


    }

    private void resetAdapter() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

        private final Context context;
        private LayoutInflater inflater;
        // private List<Correspondent> buddys;
        private OnShowChatHistoryListener listener;

        public ChatHistoryAdapter(Context context, OnShowChatHistoryListener listener) {
            this.inflater = LayoutInflater.from(context);
            // this.buddys = buddys;
            this.context = context;
            this.listener = listener;
        }

        @Override
        public int getItemCount() {

            return mLatestMsgs.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder vh, final int position) {

            try {

                SQLiteHandler db = new SQLiteHandler(getActivity());
                db.openToRead();

                String uname = db.getUsername();
                db.close();


                //final String name = mLatestMsgs.get(position).chat.sender;

                vh.tvBuddys.setText(uname.equals(mLatestMsgs.get(position).chat.sender) ? mLatestMsgs.get(position).chat.receiver : mLatestMsgs.get(position).chat.sender);

                SimpleDateFormat existingUTCFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat requiredFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                Date getDate = null;
                try {
                    getDate = existingUTCFormat.parse(mLatestMsgs.get(position).stamp);
                } catch (ParseException e) {
                    L.error(e.getMessage());
                }
                String mydate = requiredFormat.format(getDate);


                vh.tvMsg.setText(mLatestMsgs.get(position).chat.body);
                vh.tvMsgDate.setText(mydate);


                if (mLatestMsgs.get(position).avatar != null) {
                    vh.imgProfilePic.setImageBitmap(mLatestMsgs.get(position).avatar);
                }

            } catch (IndexOutOfBoundsException e) {
                L.error(e.getMessage());
            }

            vh.position = position;


        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = inflater.inflate(R.layout.row_chat_history, parent, false);
            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

            TextView tvBuddys;
            TextView tvMsg;
            TextView tvMsgDate;
            ImageView imgProfilePic;
            int position;

            public ViewHolder(View view) {
                super(view);
                tvBuddys = (TextView) view.findViewById(R.id.tv_buddys_name);
                tvMsg = (TextView) view.findViewById(R.id.tv_buddys_msg);
                tvMsgDate = (TextView) view.findViewById(R.id.tv_buddys_msg_date);
                imgProfilePic = (ImageView) view.findViewById(R.id.img_profile_pic);

                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                //L.debug("with: "+tvBuddys.getText().toString()+", start: "+tvMsg.getText().toString());
                mCallback.onShowChatHistory(tvBuddys.getText().toString());
            }

        }
    }

    public interface OnShowChatHistoryListener {
        public void onShowChatHistory(String with);
        //public void onShowChatHistory(Correspondent buddy);
    }

    @Override
    public void onCorrespondentUpdate() {

        rvChatHistory.post(new Runnable() {

            @Override
            public void run() {
                L.debug("ChatHistoryList, onCorrespondentUpdate");
                adapter.notifyDataSetChanged();
            }
        });
    }

}
