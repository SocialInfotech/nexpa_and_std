package com.lpoezy.nexpa.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.RoundedImageView;

public class PeopleProfileActivity extends Activity implements Correspondent.OnCorrespondentUpdateListener{

	private ImageView imgProfile;
	private Button btnMessage;
	private TextView txtDistance;
	private TextView txtFName;
	private TextView txtAge;
	private TextView txtGender;
	private TextView txtStatus;
	private TextView txtAboutMe;
	private TextView txtLookingType;


	String username;
	String email;
	String fname;
	String age;
	String gender;
	String distance;
	String about_me;
	String looking_type;
	String status;
	private long userId;
	private TextView mTvJobTitle;
	private TextView mTvUname;
	private TextView mTvUrl0;
	private TextView mTvUrl1;
	private TextView mTvUrl2;
	protected Correspondent mCorrespondent;
	public static boolean isRunning = false;

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

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_people_profile);

		imgProfile = (ImageView) findViewById(R.id.img_profile);
//		RoundedImageView riv = new RoundedImageView(this);
//		Bitmap rawImage = BitmapFactory.decodeResource(this.getResources(),
//		R.drawable.pic_sample_girl);
//
//		Bitmap circImage = riv.getCroppedBitmap(rawImage, 400);
//		imgProfile.setImageBitmap(circImage);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			Log.e("NOTICE", "Device cannot handle ActionBar");
		}
		
		Intent intent = getIntent();

		username = intent.getStringExtra("TAG_GEO_USER");

		distance = intent.getStringExtra("TAG_GEO_DISTANCE")+"m";

		mTvJobTitle = (TextView)this.findViewById(R.id.tv_job_title);
        mTvUname = (TextView)this.findViewById(R.id.tv_uname);
        mTvUrl0 = (TextView)this.findViewById(R.id.tv_url0);
        mTvUrl1 = (TextView)this.findViewById(R.id.tv_url1);
        mTvUrl2 = (TextView)this.findViewById(R.id.tv_url2);
		

		btnMessage = (Button) findViewById(R.id.btn_mes);
		btnMessage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent intentMes = new Intent(PeopleProfileActivity.this, ChatActivity.class);
				intentMes.putExtra("userid", userId);
				intentMes.putExtra("email", email);
				intentMes.putExtra("username", username);
				intentMes.putExtra("fname", fname);
				startActivity(intentMes);
			}
		});


	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		isRunning = true;
		resetProfilePic();
		resetUserInfo();
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		
		isRunning = false;
	}
	
	private void resetProfilePic(){
		
		String imgDecodableString = ProfilePicture.getUserImgDecodableString(this);
		
        Bitmap rawImage = BitmapFactory.decodeResource(getResources(),
        R.drawable.pic_sample_girl);
      
        RoundedImageView riv = new RoundedImageView(this);
        Bitmap circImage = riv.getCroppedBitmap(rawImage, 100);
        imgProfile.setImageBitmap(circImage);
        
        mCorrespondent = new Correspondent();
		mCorrespondent.addListener(PeopleProfileActivity.this);
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				mCorrespondent.downloadProfilePicOnline(PeopleProfileActivity.this, userId);
				
			}
		}).start();
	}
	
	private void resetUserInfo() {
		
		SQLiteHandler db = new SQLiteHandler(this);
		db.openToRead();
		
		UserProfile profile = new UserProfile();
		profile.setId(userId);
		profile.downloadOffline(this);
		
		mTvJobTitle.setVisibility(View.GONE);
		mTvUname.setVisibility(View.GONE);
		mTvUrl0.setVisibility(View.GONE);
		mTvUrl1.setVisibility(View.GONE);
		mTvUrl2.setVisibility(View.GONE);
		
		if(profile.getProfession()!=null && !profile.getProfession().equalsIgnoreCase("null") && !profile.getProfession().equals("")){
			mTvJobTitle.setVisibility(View.VISIBLE);
			mTvJobTitle.setText(profile.getProfession());
		}

		mTvUname.setVisibility(View.VISIBLE);
		mTvUname.setText(username+ " | " + distance);

		if(profile.getUrl0()!=null &&!profile.getUrl0().equalsIgnoreCase("null") && !profile.getUrl0().equals("")){
			mTvUrl0.setVisibility(View.VISIBLE);
			mTvUrl0.setText(profile.getUrl0());
		}
		
		if(profile.getUrl1()!=null &&!profile.getUrl1().equalsIgnoreCase("null") && !profile.getUrl1().equals("")){
			mTvUrl1.setVisibility(View.VISIBLE);
			mTvUrl1.setText(profile.getUrl1());
		}
		
		if(profile.getUrl2()!=null &&!profile.getUrl2().equalsIgnoreCase("null") && !profile.getUrl2().equals("")){
			mTvUrl2.setVisibility(View.VISIBLE);
			mTvUrl2.setText(profile.getUrl2());
		}
		
		db.close();
		
	}

	

	@Override
	public void onCorrespondentUpdate() {
		Bitmap rawImage = mCorrespondent.getProfilePic();
		
		RoundedImageView riv = new RoundedImageView(PeopleProfileActivity.this);
        final Bitmap circImage = riv.getCroppedBitmap(rawImage, 100);
        
        imgProfile.post(new Runnable() {
			
			@Override
			public void run() {
				
				imgProfile.setImageBitmap(circImage);
				
			}
		});
		
	}

}