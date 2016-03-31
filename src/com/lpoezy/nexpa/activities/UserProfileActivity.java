package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.lpoezy.nexpa.JSON.Profile;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.openfire.XMPPLogic;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.NiceDialog;
import com.lpoezy.nexpa.utility.Utilz;

import org.jivesoftware.smack.XMPPConnection;

public class UserProfileActivity extends AppCompatActivity
		implements EditProfileFragment.OnShowProfilePicScreenListener, XMPPService.OnServiceConnectedListener {
	RangeBar rbDistance;

	NiceDialog nd;
	LinearLayout ln_personal;
	LinearLayout ln_preference;
	LinearLayout ln_settings;
	LinearLayout ln_distance;
	Button ln_status;
	Dialog dialog;
	Dialog dialogPref;
	Dialog dialogSettings;
	Dialog dialogStatus;

	ImageView imgProfile;
	EditText edtName;
	EditText edtStatus;
	TextView txtStat;
	TextView txtCharLeft;

	RadioButton rad_lookingfor_friends;
	RadioButton rad_lookingfor_date;
	RadioButton rad_lookingfor_serious_relationship;
	RadioButton rad_lookingfor_networking;
	String strLookingForStat;

	RadioButton rad_sexual_orientation_unspecified;
	RadioButton rad_sexual_orientation_straight;
	RadioButton rad_sexual_orientation_gay_lesbian;
	RadioButton rad_sexual_orientation_bisexual;
	RadioButton rad_sexual_orientation_transgendered;
	String strSexOrien;

	RadioButton rad_looking_for_men;
	RadioButton rad_looking_for_women;
	RadioButton rad_looking_for_both;
	String strGenderPref;

	CheckBox cbx_orien_straight;
	CheckBox cbx_orien_gay;
	CheckBox cbx_orien_lesbian;
	CheckBox cbx_orien_bisexual;
	CheckBox cbx_orien_transgendered;
	CheckBox cbx_orien_unspecified;
	String strShowOrientation;

	RadioButton rad_relationship_unspecified;
	RadioButton rad_relationship_single;
	RadioButton rad_relationship_in_a_rel;
	RadioButton rad_relationship_married;
	RadioButton rad_relationship_separated;
	RadioButton rad_relationship_widowed;
	RadioButton rad_relationship_complicated;
	String strRelStat;

	RadioButton rad_chat_everyone;
	RadioButton rad_chat_friends;
	RadioButton rad_chat_noone;
	String strIndieChat;

	RadioButton rad_com_receive;
	RadioButton rad_com_dont_receive;
	String strComChat;

	EditText edOldp;
	EditText edNewp;
	EditText edVerip;
	Button btnSavePass;

	int statIndexer;
	String distTick = "";

	SQLiteHandler db;

	Profile jsonProfile;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		L.debug("UserProfileActivity, onActivityResult");
		if (profPicDialog != null)
			profPicDialog.onActivityResult(requestCode, resultCode, intent);

	}

	// @Override
	// public void onBackPressed() {
	//
	// //super.onBackPressed();
	// SessionManager session = new SessionManager(getApplicationContext());
	// if(session.isLoggedIn()){
	// UserProfileActivity.promptYesNoDialog("Quit Toucan?",
	// "Are you sure you want to log off?",
	// this,
	// "DEAC",
	// true);
	// }
	// }

	@Override
	protected void onPause() {
		super.onPause();


	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_userprofile);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
		// myToolbar.setTitle("Settings");
		TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
		mTitle.setText("SETTINGS");
		myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
		myToolbar.setTitle("");

		if (savedInstanceState == null) {
			Fragment myBroadcasts = MyBroadcastsFragment.newInstance();
			getFragmentManager().beginTransaction().add(R.id.frag_container, myBroadcasts, "MyBroadcasts").commit();
		}

	}

	private void makeNotify(CharSequence con, Style style) {
		AppMsg.makeText(this, con, style).show();
	}



	private static Dialog dialogStatusYN;
	static LinearLayout lnHeader;
	static TextView edtStatusHead;
	static TextView edtStatus1;

	private static ProgressDialog pDialog;

	@Override
	public void OnServiceConnected(XMPPService service) {

	}

	@Override
	public void OnServiceDisconnected() {

	}


	public interface onUserIsLoggedOutListener {
		public void onUserIsLoggedOut();
	}

	ProfilePicFragment profPicDialog;

	@Override
	public void onShowProfilePicScreen() {
		profPicDialog = ProfilePicFragment.newInstance();

		profPicDialog.show(getFragmentManager().beginTransaction(), ProfilePicFragment.TAG);

	}

}