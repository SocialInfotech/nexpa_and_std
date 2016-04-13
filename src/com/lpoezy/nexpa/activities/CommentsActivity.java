package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.utility.L;

public class CommentsActivity extends AppCompatActivity {

    public static boolean isRunning;

    @Override
    protected void onPause() {
        super.onPause();

        isRunning= false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        isRunning = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        String broadcastId = getIntent().getStringExtra(CommentsFragment.BROADCAST_ID);
        String address = getIntent().getStringExtra(CommentsFragment.ADDRESS);

        if (savedInstanceState == null) {
            Fragment commentsFrag = CommentsFragment.newInstance(broadcastId, address);
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
