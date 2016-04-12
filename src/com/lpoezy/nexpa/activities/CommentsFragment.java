package com.lpoezy.nexpa.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.BroadcastComment;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.xdata.packet.DataForm;


public class CommentsFragment extends Fragment {

    public static final String BROADCAST_ID = "BROADCAST_ID";
    public static final String UNAME = "UNAME";
    public static final String ADDRESS = "ADDRESS";
    private String mBroadcastId;
    private String mAddress;

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

        L.debug("mBroadcastId: " + mBroadcastId);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class OnSendCommentTask implements OnExecutePendingTaskListener {
        @Override
        public void onExecutePendingTask() {

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
                    node = mgr.getNode(mBroadcastId + "-broadcast");
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

                if(node!=null){
                    String from = db.getUsername();
                    String address = mAddress;
                    String date = Long.toString(System.currentTimeMillis());
                    String body = "this is a comment!!!";
                    BroadcastComment comment = new BroadcastComment(from, body, address, date);

                    Gson gson = new Gson();
                    try {
                        node.send(new PayloadItem(null,
                                new SimplePayload("broadcast", "pubsub:nexpa:broadcast", "<comment xmlns='pubsub:nexpa:comment'>" + gson.toJson(comment) + "</comment>")));
                    } catch (SmackException.NoResponseException e) {
                        L.error(e.getMessage());
                    } catch (XMPPException.XMPPErrorException e) {
                        L.error(e.getMessage());
                    } catch (SmackException.NotConnectedException e) {
                        L.error(e.getMessage());
                    }

                }

                db.close();

            }
        }
    }

    ;

    private LeafNode createCommentNode() throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        LeafNode node = null;

        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);


        ConfigureForm form = new ConfigureForm(DataForm.Type.form.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);

        node = (LeafNode) mgr.createNode(mBroadcastId + "-broadcast", form);


        return node;

    }

}




