package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.utility.L;

public class CommentsActivity extends AppCompatActivity {

    public static boolean isRunning;

    @Override
    protected void onPause() {
        super.onPause();

        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }

        isRunning= false;
    }

    public boolean mBounded;
    public XMPPService mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mService = null;

            CommentsFragment frag = (CommentsFragment)getFragmentManager().findFragmentByTag("Comments");
            frag.onServiceDisconnected(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;

            mService = ((LocalBinder<XMPPService>) service).getService();
            //L.debug("CommentsActivity, onServiceConnected");
            CommentsFragment frag = (CommentsFragment)getFragmentManager().findFragmentByTag("Comments");
            frag.onServiceConnected(name,  service);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        isRunning = true;

        Intent service = new Intent(this, XMPPService.class);
        startService(service);
        bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        String broadcastId = getIntent().getStringExtra(CommentsFragment.BROADCAST_ID);
        String address = getIntent().getStringExtra(CommentsFragment.ADDRESS);
        String uname = getIntent().getStringExtra(CommentsFragment.UNAME);
        String date = getIntent().getStringExtra(CommentsFragment.DATE);
        String broadcast = getIntent().getStringExtra(CommentsFragment.BROADCAST);

        if (savedInstanceState == null) {
            Fragment commentsFrag = CommentsFragment.newInstance(broadcastId, address, uname, date, broadcast);
            getFragmentManager().beginTransaction()
                    .add(R.id.frag_container, commentsFrag, "Comments")
                    .commit();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("COMMENTS");
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));

    }

}
