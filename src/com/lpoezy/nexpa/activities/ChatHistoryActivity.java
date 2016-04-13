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



        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("MESSAGES");
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        myToolbar.setTitle("");
    }

    @Override
    protected void onPause() {


        ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
        XMPPService.OnServiceConnectedListener onServiceConnectedListener = (XMPPService.OnServiceConnectedListener) frag;
        onServiceConnectedListener.OnServiceDisconnected();

        super.onPause();

        isRunning = false;
    }

    @Override
    protected void onResume() {

        super.onResume();

        isRunning = true;

        L.debug("ChatHistoryACtivity, onREsume");

        if (((TabHostActivity) getParent()).getService()!=null) {
            ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
            XMPPService.OnServiceConnectedListener onServiceConnectedListener = (XMPPService.OnServiceConnectedListener) frag;
            onServiceConnectedListener.OnServiceConnected(((TabHostActivity) getParent()).getService());
        }

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


    protected XMPPService mService;
    protected boolean mBounded;
    @Override
    public void OnServiceConnected(XMPPService service) {

        mService = service;
        mBounded = true;

        ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
        XMPPService.OnServiceConnectedListener onServiceConnectedListener = (XMPPService.OnServiceConnectedListener) frag;
        onServiceConnectedListener.OnServiceConnected(mService);

    }

    @Override
    public void OnServiceDisconnected() {

        mService = null;
        mBounded = false;

        ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
        XMPPService.OnServiceConnectedListener onServiceConnectedListener = (XMPPService.OnServiceConnectedListener) frag;
        onServiceConnectedListener.OnServiceDisconnected();


    }
}
