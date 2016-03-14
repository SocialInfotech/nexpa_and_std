package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.L;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;

public class MainSignInActivity extends Activity {
	private static final String TAG = MainSignInActivity.class.getSimpleName();
	private Button btnLogin;
	private Button btnLinkToRegister;
	private Button btnLinkToForgotPassword;
	private EditText inputEmail;
	private EditText inputPassword;
	private ProgressDialog pDialog;
	private SessionManager session;
	// private LoginButton loginBtn;
	private TextView username;
	// private UiLifecycleHelper uiHelper;
	private SQLiteHandler db;
	Bitmap proPic;
	private ImageView imgProfile;
	private ImageView imgRotator;
	private ImageView imgRotatorB;
	private ImageView imgRotatorDark;
	private ImageView imgRotatorDarkB;
	private Animation animScrollLeftSlow;
	private Animation animScrollLeftSlowB;
	private Animation animScrollLeft;
	private Animation animScrollLeftB;
	Timer timer;

	String server_uid;
	String server_name;
	String server_email;
	String server_created_at;
	String public_pass;
	protected boolean mBounded;
	protected XMPPService mService;

	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBounded = false;
			mService = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBounded = true;

			mService = ((LocalBinder<XMPPService>) service).getService();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_sign_in);
		imgRotator = (ImageView) findViewById(R.id.rotator_disp);
		imgRotatorB = (ImageView) findViewById(R.id.rotator_disp_b);
		imgRotatorDark = (ImageView) findViewById(R.id.rotator_disp_2);
		imgRotatorDarkB = (ImageView) findViewById(R.id.rotator_disp_2_b);
		animScrollLeftSlow = AnimationUtils.loadAnimation(this, R.anim.anim_scroll_slow);
		animScrollLeft = AnimationUtils.loadAnimation(this, R.anim.anim_scroll);
		animScrollLeftB = AnimationUtils.loadAnimation(this, R.anim.anim_scroll_b);
		animScrollLeftSlowB = AnimationUtils.loadAnimation(this, R.anim.anim_scroll_slow_b);
		imgRotator.startAnimation(animScrollLeft);
		imgRotatorB.startAnimation(animScrollLeftB);
		imgRotatorDark.startAnimation(animScrollLeftSlow);
		imgRotatorDarkB.startAnimation(animScrollLeftSlowB);
		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.password);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		btnLinkToForgotPassword = (Button) findViewById(R.id.btnLinkToForgotPassword);
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		session = new SessionManager(getApplicationContext());
		db = new SQLiteHandler(this);
		db.openToWrite();
		final CreateAccountActivity cAct;

		btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String username = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
				if (username.trim().length() > 0 && password.trim().length() > 0) {

					if (mBounded) {

						checkLogin(username, password);

					} else {

						L.error("service not yet available");
					}

				} else {
					L.makeText(MainSignInActivity.this, "Please enter your username and password.", AppMsg.STYLE_ALERT);
				}
			}
		});
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), CreateAccountActivity.class);
				startActivity(i);
				finish();
			}
		});

		btnLinkToForgotPassword.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
				startActivity(i);
				// finish();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		Intent service = new Intent(this, XMPPService.class);
		//startService(service);
		bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mServiceConn != null) {

			unbindService(mServiceConn);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		// uiHelper.onSaveInstanceState(savedState);
	}

	private void makeNotify(CharSequence con, Style style) {

		AppMsg.makeText(this, con, style).show();
	}


	private void checkLogin(final String uname, final String password) {

		final String tag_string_req = "login";
		pDialog.setMessage("Logging in ...");
		showDialog();

		mService.login(uname, password, new XMPPService.OnUpdateScreenListener() {

			@Override
			public void onUpdateScreen() {

				hideDialog();

				Intent act = new Intent(MainSignInActivity.this, TabHostActivity.class);
				startActivity(act);
				finish();
				//L.makeText(MainSignInActivity.this, "you logged in successfully.", AppMsg.STYLE_INFO);
			}

			@Override
			public void onResumeScreen(String errorMsg) {

				hideDialog();

				L.makeText(MainSignInActivity.this, errorMsg, AppMsg.STYLE_ALERT);
			}
		});

	}

	private void saveBitmap(Bitmap bitmap) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
		File f = new File(getCacheDir() + File.separator + "test.jpg");
		Log.e("", getCacheDir() + " bb");
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			fo.write(bytes.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {

		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				if (pDialog.isShowing())
					pDialog.dismiss();

			}
		});

	}
}