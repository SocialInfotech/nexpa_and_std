package com.lpoezy.nexpa.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.activities.ChatHistoryListFragment.OnShowChatHistoryListener;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.MAMExtensionProvider;
import com.lpoezy.nexpa.objects.MAMFinExtensionProvider;
import com.lpoezy.nexpa.objects.MessageArchiveWithIQ;
import com.lpoezy.nexpa.objects.MessageResultElement;
import com.lpoezy.nexpa.objects.OnRetrieveMessageArchiveListener;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;

import java.util.ArrayList;
import java.util.List;


public class ChatHistoryActivity extends AppCompatActivity implements OnShowChatHistoryListener, OnRetrieveMessageArchiveListener, XMPPManager.OnConnectedToOPenfireListener, XMPPService.OnServiceConnectedListener {

    public static boolean isRunning;

//	@Override
//	public void onBackPressed() {
//		
//		//super.onBackPressed();
//		SessionManager session = new SessionManager(getApplicationContext());
//		if(session.isLoggedIn()){
//			UserProfileActivity.promptYesNoDialog("Quit Toucan?",
//					"Are you sure you want to log off?",
//   					this,
//   					"DEAC",
//   					true);
//		}
//	}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);


        if (savedInstanceState == null) {
            Fragment chatHistoryList = ChatHistoryListFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.frag_container, chatHistoryList, "ChatHistoryList")
                    .commit();
        }


//		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
//			 ActionBar actionBar = getActionBar();
//			 actionBar.setHomeButtonEnabled(true);
//			 actionBar.setDisplayHomeAsUpEnabled(true);
//		}
//		else{
//			 Log.e("NOTICE","Device cannot handle ActionBar");
//		}


        //RecyclerView
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("MESSAGES");
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        myToolbar.setTitle("");
    }

    @Override
    protected void onPause() {


        if (((TabHostActivity) getParent()).getService()!=null) {
            //((TabHostActivity) getParent()).getService().removeMAMObserver(this);
            //((TabHostActivity) getParent()).getService().removeOnConnectedToOpenfireObserver(this);

        }

        super.onPause();

        isRunning = false;
    }

    @Override
    protected void onResume() {

        super.onResume();

        isRunning = true;

        L.debug("ChatHistoryACtivity, onREsume");

        if (((TabHostActivity) getParent()).isBounded()) {
            L.debug("ChatHistoryACtivity, isBounded");
            //((TabHostActivity) getParent()).getService().addMAMObserver(this);
            //((TabHostActivity)getParent()).getService().addOnConnectedToOpenfireObserver(this);
            //((TabHostActivity) getParent()).getService().retrieveListOfCollectionsFrmMsgArchive(null);

        }
        
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                AbstractXMPPConnection connection = XMPPService.xmpp.connection;
//
//                if(connection.isAuthenticated()) {
//                    MessageArchiveWithIQ mam = new MessageArchiveWithIQ(null);
//                    mam.setType(IQ.Type.set);
//                    try {
//                        connection.sendStanza(mam);
//                    } catch (SmackException.NotConnectedException e) {
//                        L.error("retrieveListOfCollectionsFrmMsgArchive: " + e.getMessage());
//
//                    }
//
//                    final List<MessageResultElement> msgElements = new ArrayList<MessageResultElement>();
//
//                    ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0",
//                            new MAMExtensionProvider(
//                                    new MessageResultElement.OnParseCompleteListener() {
//
//                                        @Override
//                                        public void onParseComplete(MessageResultElement msg) {
//
//                                            //L.debug("msgs: "+msgs.size());
//
//                                            msgElements.add(msg);
//                                        }
//                                    }
//                            ));
//
//                    ProviderManager.addExtensionProvider("fin", "urn:xmpp:mam:0",
//                            new MAMFinExtensionProvider(
//                                    new MAMFinExtensionProvider.OnParseCompleteListener() {
//
//                                        @Override
//                                        public void onParseComplete(final int first, final int last, final int count) {
//
//                                            L.debug("msgs: " + msgElements.size() + ", onParseComplete: first: " + first + ", last: " + last + ", count: " + count);
//                                            //notifyMAMListeners(msgElements, first, last, count);
//                                            onRetrieveMessageArchive(msgElements, first, last, count);
//
//                                            final ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
//                                            frag.updateUI();
//
//                                        }
//                                    }
//                            ));
//                }else if(!connection.isConnected()){
//                    XMPPService.xmpp.connect("onCreate");
//                }else{
//                    XMPPService.xmpp.login();
//                }
//
//
//
//
//            }
//        }).start();
    }

    @Override
    public void onShowChatHistory(String with) {

        Intent intentMes = new Intent(this, ChatActivity.class);
        intentMes.putExtra("with", with + "@198.154.106.139");
        startActivity(intentMes);

    }


    @Override
    public void onRetrieveMessageArchive(List<MessageResultElement> msgs, int first, int last, int count) {

    }

    @Override
    public void onConnectedToOpenfire(XMPPConnection connection) {

    }


    private XMPPService mService;
    private boolean mBounded;
    @Override
    public void OnServiceConnected(XMPPService service) {

        mService = service;
        mBounded = true;
        mService.addMAMObserver(this);
        mService.addOnConnectedToOpenfireObserver(ChatHistoryActivity.this);

    }

    @Override
    public void OnServiceDisconnected() {

        mService = null;
        mBounded = false;


    }
}
