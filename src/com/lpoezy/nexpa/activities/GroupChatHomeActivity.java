package com.lpoezy.nexpa.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.ArrayList;
import java.util.List;

public class GroupChatHomeActivity extends AppCompatActivity {


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("WINDOW", "PAUSED ");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_home);

        new Thread(new Runnable() {
            @Override
            public void run() {


                // Create a pubsub manager using an existing XMPPConnection
                PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                LeafNode node = null;
                try {
                    node = mgr.getNode("testNode");

                    ConfigureForm configureForm = new ConfigureForm(DataForm.Type.submit);
                    configureForm.setAccessModel(AccessModel.open);
                    configureForm.setDeliverPayloads(false);
                    configureForm.setNotifyRetract(true);
                    configureForm.setPersistentItems(true);
                    configureForm.setPublishModel(PublishModel.open);

                    node.sendConfigurationForm(configureForm);

                    // Publish an Item with payload
//                    node.send(new PayloadItem(null,
//                            new SimplePayload("book", "pubsub:test:book", "<book xmlns='pubsub:test:book'>text book</book>")));
                } catch (SmackException.NoResponseException e) {
                    L.error(e.getMessage());
                } catch (XMPPException.XMPPErrorException e) {
                    L.error(e.getMessage());
                } catch (SmackException.NotConnectedException e) {
                    L.error(e.getMessage());
                }


                node.addItemEventListener(new ItemEventCoordinator());
                try {
                    node.subscribe("kato@198.154.106.139");
                } catch (SmackException.NoResponseException e) {
                    L.error(e.getMessage());
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    L.error(e.getMessage());
                }

            }
        }).start();

    }

    class ItemEventCoordinator  implements ItemEventListener {

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            L.debug("Item count: " + items);



        }
    }
}