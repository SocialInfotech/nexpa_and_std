package com.lpoezy.nexpa.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.lpoezy.nexpa.JSON.Profile;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.openfire.XMPPLogic;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.NiceDialog;
import com.lpoezy.nexpa.utility.Utilz;

import org.jivesoftware.smack.XMPPConnection;

public class SettingsActivity extends Activity {
	RangeBar rbDistance;

	NiceDialog nd;
	LinearLayout ln_personal;
	// LinearLayout ln_preference;
	LinearLayout ln_settings;
	LinearLayout In_app_purchase;
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

	private DatePicker dpBDay;

	private Button btnLogout;

	SQLiteHandler db;
	private SessionManager session;
	Profile jsonProfile;

	protected ImageView mProfilePic;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; goto parent activity.
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {

		super.onPause();

	}

	@Override
	protected void onResume() {

		super.onResume();
//		if (dialog != null && dialog.isShowing()) {
//			resetProfilePic();
//		}

	}



	@Override
	protected void onDestroy() {

		super.onDestroy();

		db.close();
		db = null;
	}


	private ProgressDialog pDialog;

	private LinearLayout ln_sync;

	private LinearLayout ln_signout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			Log.e("NOTICE", "Device cannot handle ActionBar");
		}

		db = new SQLiteHandler(this);
		db.openToWrite();

		In_app_purchase = (LinearLayout) findViewById(R.id.In_app_purchase);
		In_app_purchase.setVisibility(View.GONE);
		In_app_purchase.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SettingsActivity.this);
				builder.setTitle("In App Purchases");
				builder.setMessage("Premium user is coming soon..");
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								Log.e("info", "OK");
							}
						});
				builder.show();
			}
		});

		ln_signout = (LinearLayout) findViewById(R.id.ln_sign_out);
		ln_signout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				SessionManager session = new SessionManager(getApplicationContext());
				if(session.isLoggedIn()){
					promptYesNoDialog("Quit Nexpa?",
							"Are you sure you want to log off?",
							SettingsActivity.this,
							"DEAC",
							true);
				}

			}
		});
	}

	public void logoutUser(Context context) {

		stopService(new Intent(SettingsActivity.this, XMPPService.class));
		//

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			L.error(e.getMessage());
		}

		SessionManager session = new SessionManager(context);
		SessionManager sm = new SessionManager(SettingsActivity.this);
		sm.clearSession();

		// Launching the login activity
		// if(!isExitingApp){
		Intent intent = new Intent(context, MainSignInActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

		// }
		pDialog.dismiss();
		((Activity) context).finish();
	}

	private void promptYesNoDialog(final String caption, final String message, final Context cn,
								   final String fcType, final boolean isExitingApp) {

		final Dialog dialogStatusYN = new Dialog(cn);
		dialogStatusYN.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogStatusYN.setContentView(R.layout.dialog_yesno);
		final Button dialogButton = (Button) dialogStatusYN.findViewById(R.id.dialogButtonYes);
		final Button dialogButtonNo = (Button) dialogStatusYN.findViewById(R.id.dialogButtonNo);
		LinearLayout lnHeader = (LinearLayout) dialogStatusYN.findViewById(R.id.lnHeader);
		TextView edtStatusHead = (TextView) dialogStatusYN.findViewById(R.id.edtStatusHead);
		TextView edtStatus1 = (TextView) dialogStatusYN.findViewById(R.id.edtStatus);

		edtStatusHead.setText(caption);
		edtStatus1.setText(message);

		dialogButton.setBackgroundColor(cn.getResources().getColor(R.color.toucan_yellow));
		dialogButtonNo.setBackgroundColor(cn.getResources().getColor(R.color.toucan_yellow));
		lnHeader.setBackgroundColor(cn.getResources().getColor(R.color.toucan_yellow));

		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				pDialog = new ProgressDialog(SettingsActivity.this);
				pDialog.setCancelable(false);
				pDialog.setMessage("Logging out ...");
				pDialog.show();

				logoutUser(SettingsActivity.this);


				// dismiss quit toucan dialog
				dialogStatusYN.dismiss();

			}
		});

		dialogButtonNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogStatusYN.dismiss();
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialogStatusYN.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialogStatusYN.show();
		dialogStatusYN.getWindow().setAttributes(lp);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

//	private void resetProfilePic() {
//		ImageView profilePic = (ImageView) dialog.findViewById(R.id.img_profile_pic);
//
//		if (profilePic != null) {
//			//String imgDecodableString = Utilz.getDataFrmSharedPref(SettingsActivity.this, UserProfile.PROFILE_PIC_LOC, "");
//
//			long userId = -1;
//			SQLiteHandler db = new SQLiteHandler(SettingsActivity.this);
//			db.openToRead();
//			userId = Long.parseLong(db.getLoggedInID());
//			db.close();
//
//			ProfilePicture pic = new ProfilePicture();
//			//pic.setUserId(userId);
//			pic.downloadOffline(SettingsActivity.this);
//
//
//			String imgDecodableString = pic.getImgDir()+"/"+pic.getImgFile();
//
//			Bitmap rawImage = BitmapFactory.decodeResource(getResources(),
//					R.drawable.pic_sample_girl);
//
//
//			if ((pic.getImgDir()!=null && !pic.getImgDir().isEmpty())
//					&& (pic.getImgFile()!=null && !pic.getImgFile().isEmpty())) {
//				L.debug("SettingsActivity, imgDecodableString "+imgDecodableString);
//				// Get the dimensions of the View
//				int targetW = profilePic.getWidth();
//				int targetH = profilePic.getHeight();
//
//				BmpFactory  bmpFactory = new BmpFactory();
//				rawImage = bmpFactory.getBmpWithTargetWTargetHFrm(targetW, targetH, imgDecodableString);
//			}
//
//			profilePic.setImageBitmap(rawImage);
//		}
//
//	}

}