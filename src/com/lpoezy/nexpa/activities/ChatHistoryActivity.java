package com.lpoezy.nexpa.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.activities.ChatHistoryListFragment.OnShowChatHistoryListener;
import com.lpoezy.nexpa.objects.MessageResultElement;
import com.lpoezy.nexpa.objects.OnRetrieveMessageArchiveListener;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import java.util.List;


public class ChatHistoryActivity extends AppCompatActivity implements OnShowChatHistoryListener, OnRetrieveMessageArchiveListener {

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


        if (((TabHostActivity) getParent()).isBounded()) {
            ((TabHostActivity) getParent()).getService().removeMAMObserver(this);
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
            ((TabHostActivity) getParent()).getService().addMAMObserver(this);

            ((TabHostActivity) getParent()).getService().retrieveListOfCollectionsFrmMsgArchive(null);

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

        if (msgs != null && !msgs.isEmpty()) {
            L.debug("ChatHistoryActivity, onParseComplete");

            SQLiteHandler db = new SQLiteHandler(ChatHistoryActivity.this);
            db.openToWrite();

            //save messages to offline db
            db.deleteMsgArchive();
            db.saveMsgArchive(msgs);

            db.close();
        }

        final ChatHistoryListFragment frag = (ChatHistoryListFragment) getFragmentManager().findFragmentByTag("ChatHistoryList");
        frag.updateUI();

    }
}
