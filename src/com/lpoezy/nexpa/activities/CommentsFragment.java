package com.lpoezy.nexpa.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.BroadcastComment;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.L;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class CommentsFragment extends Fragment {

    public static final String BROADCAST_ID = "BROADCAST_ID";
    public static final String UNAME = "UNAME";
    public static final String ADDRESS = "ADDRESS";
    private String mBroadcastId;
    private String mAddress;
    private static CommentsAdapter adapter;
    private RecyclerView mRvComments;
    private  SwipyRefreshLayout mSwipeRefreshLayout;

    public CommentsFragment() {
        // Required empty public constructor
    }

    public static CommentsFragment newInstance(String itemid, String address) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(BROADCAST_ID, itemid);
        args.putString(ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (View) inflater.inflate(R.layout.fragment_comments, container, false);

        if (getArguments() != null) {
            mBroadcastId = getArguments().getString(BROADCAST_ID);
            mAddress  = getArguments().getString(ADDRESS);
        }

        mGson = new Gson();
        comments = new ArrayList<BroadcastComment>();
        
        adapter = new CommentsAdapter(getActivity());

        mRvComments = (RecyclerView) v.findViewById(R.id.listComments);
        mRvComments.setAdapter(adapter);

        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
//
//        lm.setStackFromEnd(true);
        mRvComments.setLayoutManager(lm);
        //L.debug("mBroadcastId: " + mBroadcastId);


        mSwipeRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.niagara, R.color.buttercup, R.color.niagara);
        mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.carrara));


        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        RetrieveMyOwnBroadcast task = new RetrieveMyOwnBroadcast();
        task.onExecutePendingTask();
        super.onResume();
    }

    private static List<BroadcastComment> comments;

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

        private final Context context;
        private final LayoutInflater inflater;

        public CommentsAdapter(Context context) {

            L.debug("================CommentsAdapter=================");
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = inflater.inflate(R.layout.list_comment, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, int position) {

            BroadcastComment bc = comments.get(position);

            vh.tvCommentMsg.setText(bc.getBody());
            vh.tvCommentFrm.setText(bc.getFrom());

            if(bc.getDate()!=null&& !bc.getDate().isEmpty()){

                vh.tvCommentDate.setText(DateUtils.millisToSimpleDate(Long.parseLong(bc.getDate()), DateUtils.DateFormatz.DATE_FORMAT_5));
            }

            vh.tvLocLocal.setVisibility(View.GONE);

            if (bc.getAddress() != null && !bc.getAddress().isEmpty()) {
                String strLoc = "near " + bc.getAddress();

                vh.tvLocLocal.setText(strLoc);
                vh.tvLocLocal.setVisibility(TextView.VISIBLE);
            }

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    0, ActionBar.LayoutParams.WRAP_CONTENT, 1.2f);
            //vh.btnReply.setLayoutParams(param);

        }

        @Override
        public int getItemCount() {
            //L.debug("getItemCount: "+comments.size());
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView tvCommentId;
            TextView tvCommentFrm;
            TextView tvCommentDate;
            TextView tvLocLocal;
            TextView tvCommentMsg;


            public ViewHolder(View itemView) {
                super(itemView);

                tvCommentId = (TextView) itemView.findViewById(R.id.comment_id);
                tvCommentFrm = (TextView) itemView.findViewById(R.id.comment_from);
                tvCommentDate = (TextView) itemView.findViewById(R.id.comment_date);
                tvLocLocal = (TextView) itemView.findViewById(R.id.location_local);
                tvCommentMsg = (TextView) itemView.findViewById(R.id.comment_message);

            }
        }
    };
    private Gson mGson;
    private class RetrieveMyOwnBroadcast implements OnExecutePendingTaskListener{
        @Override
        public void onExecutePendingTask() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!XMPPService.xmpp.connection.isConnected()) {

                        XMPPManager.getInstance(getActivity()).instance = null;

                        XMPPService.xmpp = XMPPManager.getInstance(getActivity());

                        XMPPService.xmpp.connect("onCreate");

                    } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                        XMPPService.xmpp.login();
                    } else {

                        SQLiteHandler db = new SQLiteHandler(getActivity());
                        db.openToRead();
                        // Create a pubsub manager using an existing XMPPConnection
                        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                        LeafNode node = null;

                        try {

                            node = mgr.getNode(mBroadcastId + "-comment");

                            //node.subscribe(db.getUsername() + "@198.154.106.139");
                            List<Subscription> subs = node.getSubscriptions();
                            String mySubid = null;
                            String uname = db.getUsername();
                            for(int i = 0; i < subs.size(); i++)
                            {
                                //L.debug(XMPPService.xmpp.connection.getUser()+", "+subs.get(i).getJid()+": "+subs.get(i).getId());
                                if(subs.get(i).getJid().split("@")[0].equals(uname)){
                                    mySubid = subs.get(i).getId();
                                    break;
                                }

                            }

                            //L.debug("mySubid: "+mySubid);
                            Collection<PayloadItem<Item>> eventItems = node.getItems(100, mySubid);

                            Gson gson = new Gson();
                            List<BroadcastComment>bComments = new ArrayList<BroadcastComment>();
                            for(Item item : eventItems) {


                                String comment = null;
                                String itemId = null;
                                XmlPullParserFactory factory = null;
                                try {
                                    factory = XmlPullParserFactory.newInstance();


                                    factory.setNamespaceAware(true);
                                    XmlPullParser xpp = factory.newPullParser();

                                    xpp.setInput( new StringReader( item.toXML()) );
                                    int eventType = xpp.getEventType();

                                    while (eventType != XmlPullParser.END_DOCUMENT) {
                                        if(eventType == XmlPullParser.START_DOCUMENT) {
                                            // L.debug("Start document");
                                        } else if(eventType == XmlPullParser.START_TAG) {
                                            //L.debug("Start tag " + xpp.getName());
                                            switch (xpp.getName()){
                                                case "item":
                                                    itemId = xpp.getAttributeValue("", "id");
                                                    break;
                                                case "comment":
                                                    comment = xpp.nextText();
                                                    break;
                                            }


                                        } else if(eventType == XmlPullParser.END_TAG) {
                                            // L.debug("End tag " + xpp.getName());
                                        } else if(eventType == XmlPullParser.TEXT) {
                                            //L.debug("Texxt " + xpp.getText());
                                        }
                                        eventType = xpp.next();
                                    }

                                    //L.debug("End document");
                                } catch (XmlPullParserException e) {
                                    //L.error(e.getMessage());
                                } catch (IOException e) {
                                    L.error(e.getMessage());
                                }

                                L.debug("comment: "+comment);
                                BroadcastComment bc = mGson.fromJson(comment, BroadcastComment.class);
                                bComments.add(bc);
                            }

                            if(!bComments.isEmpty()){
                                comments.clear();
                                comments.addAll(bComments);
                                Collections.reverse(comments);
                            }
                            L.debug("comments: "+comments.size());
//                            mAnouncements.clear();
//                            mAnouncements.addAll(announcements);
//                            Collections.reverse(mAnouncements);



                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });

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

        }
    };

    private class OnSendCommentTask implements OnExecutePendingTaskListener {
        @Override
        public void onExecutePendingTask() {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    L.debug("OnSendCommentTask");

                    if (!XMPPService.xmpp.connection.isConnected()) {

                        XMPPManager.getInstance(getActivity()).instance = null;

                        XMPPService.xmpp = XMPPManager.getInstance(getActivity());
                        XMPPService.xmpp.connect("onCreate");

                    } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                        XMPPService.xmpp.login();

                    } else {
                        SQLiteHandler db = new SQLiteHandler(getActivity());
                        db.openToRead();
                        // Create a pubsub manager using an existing XMPPConnection
                        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                        LeafNode node = null;

                        //PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);
                        try {
                            node = mgr.getNode(mBroadcastId + "-comment");
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
                                    if(subs.get(i).getJid().split("@")[0].equals(db.getUsername())){
                                        L.debug(db.getUsername()+" is already subscribed to "+node.getId());
                                        isAlreadySubscribe = true;
                                        break;
                                    }

                                }
                            } else {
                                L.error("sbscription is empty!!");
                                isAlreadySubscribe = false;
                            }

                            if(!isAlreadySubscribe){
                                try {
                                    node.subscribe(db.getUsername() + "@198.154.106.139");
                                    isAlreadySubscribe = true;
                                    L.debug("subscribe to "+node.getId());
                                } catch (SmackException.NoResponseException e) {
                                    L.error(e.getMessage());
                                } catch (XMPPException.XMPPErrorException e) {
                                    L.error(e.getMessage());
                                } catch (SmackException.NotConnectedException e) {
                                    L.error(e.getMessage());
                                }
                            }

//                            String from = db.getUsername();
//                            String address = mAddress;
//                            String date = Long.toString(System.currentTimeMillis());
//                            String body = "this is a comment!!!";
//                            BroadcastComment comment = new BroadcastComment(from, body, address, date, true);
//
//                            Gson gson = new Gson();
//                            try {
//                                node.send(new PayloadItem(null,
//                                        new SimplePayload("broadcast", "pubsub:nexpa:broadcast", "<comment xmlns='pubsub:nexpa:comment'>" + gson.toJson(comment) + "</comment>")));
//                            } catch (SmackException.NoResponseException e) {
//                                L.error(e.getMessage());
//                            } catch (XMPPException.XMPPErrorException e) {
//                                L.error(e.getMessage());
//                            } catch (SmackException.NotConnectedException e) {
//                                L.error(e.getMessage());
//                            }

                        }

                        db.close();
                    }

                }
            }).start();


        }
    };

    private LeafNode createCommentNode() throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        LeafNode node = null;

        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);


        ConfigureForm form = new ConfigureForm(DataForm.Type.form.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);

        node = (LeafNode) mgr.createNode(mBroadcastId + "-comment", form);


        return node;

    }

}




