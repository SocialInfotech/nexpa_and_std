package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.DateUtils.DateFormatz;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.Utilz;

public class EditProfileFragment extends DialogFragment {

    public static final String TAG = "EditProfileFragment";
    private Bitmap rawImage;
    private String mUname;

    public static EditProfileFragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }

    protected ProgressDialog pDialog;
    private EditText edtName;
    private DatePicker dpBDay;
    private ImageView profilePic;
    private RadioButton radBoy;
    private RadioButton radGirl;
    private OnShowProfilePicScreenListener mCallback;
    private EditText edtDescription;
    private EditText edtProfession;
    private EditText edtUrl0;
    private EditText edtUrl1;
    private EditText edtUrl2;
    private ProfilePicture mProfilePicture;

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//	      ProfilePicFragment fragment = (ProfilePicFragment) getChildFragmentManager().findFragmentByTag(ProfilePicFragment.TAG);
//	      if(fragment != null){
//	            fragment.onActivityResult(requestCode, resultCode, intent);
//	      }

        L.debug("EditProfileFragment, onActivityResult");
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        try {
            mCallback = (OnShowProfilePicScreenListener) activity;
        } catch (ClassCastException e) {
            L.error("" + e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        L.debug("Loading ...");

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");
        pDialog.show();
        //resetProfilePic();
        //resetProfileInfo();
        if(((TabHostActivity) getActivity().getParent()).getService()!=null){
            ((TabHostActivity) getActivity().getParent()).getService().onExecutePendingTask(new OnResetProfileInfo());
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        //let all the screens the listaening screens,
        //that there is a change in,
        //user profile information,
        //and give the listening screens to update
        getActivity().sendBroadcast(new Intent(AppConfig.ACTION_USER_PROFILE_UPDATED));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_profile_personal, container, false);

        profilePic = (ImageView) v.findViewById(R.id.img_profile_pic);


        addClickListenerToBtnProfilePic(v);

        addClickListenerToBtnOk(v);

        edtName = (EditText) v.findViewById(R.id.edtName);
        edtName.setEnabled(false);

        edtDescription = (EditText) v.findViewById(R.id.edt_short_description);
        edtProfession = (EditText) v.findViewById(R.id.edt_profession);
        edtProfession.setVisibility(View.GONE);
        edtUrl0 = (EditText) v.findViewById(R.id.edt_url0);
        edtUrl1 = (EditText) v.findViewById(R.id.edt_url1);
        edtUrl2 = (EditText) v.findViewById(R.id.edt_url2);

        SQLiteHandler db = new SQLiteHandler(getActivity());
        db.openToRead();
        mUname = db.getUsername();
        db.close();


        return v;
    }

    private void addClickListenerToBtnOk(View v) {

        ((Button) v.findViewById(R.id.dialogButtonOK)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setMessage("Saving ...");
                pDialog.show();

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //this will make sure,
                        //that the progress will be visible
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            L.error("" + e);
                        }

                        String uname = edtName.getText().toString();
                        String description = edtDescription.getText().toString();
                        String profession = edtProfession.getText().toString();
                        String url0 = edtUrl0.getText().toString();
                        String url1 = edtUrl1.getText().toString();
                        String url2 = edtUrl2.getText().toString();
                        String avatarDir = Utilz.getDataFrmSharedPref(getActivity().getApplicationContext(), UserProfile.AVATAR_DIR, "");

                        UserProfile userProfile = new UserProfile(uname, description, profession, url0, url1, url2, avatarDir);
                        userProfile.saveVCard(XMPPService.xmpp.connection);
                        userProfile.saveOffline(getActivity());

                        profilePic.post(new Runnable() {

                            @Override
                            public void run() {

                                Utilz.saveToSharedPref(getActivity(), UserProfile.AVATAR_DIR, "");
                                pDialog.dismiss();
                                pDialog = null;
                            }
                        });
                    }
                }).start();

            }
        });

    }

    private void addClickListenerToBtnProfilePic(View v) {
        ((LinearLayout) v.findViewById(R.id.btn_profile_pic)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCallback.onShowProfilePicScreen();

            }
        });

    }

    protected XMPPService mService;
    protected boolean mBounded;



    private class OnResetProfileInfo implements OnExecutePendingTaskListener{
        @Override
        public void onExecutePendingTask() {
            if (!XMPPService.xmpp.connection.isConnected()) {

                XMPPManager.getInstance(getActivity()).instance = null;

                XMPPService.xmpp = XMPPManager.getInstance(getActivity());

                XMPPService.xmpp.connect("onCreate");

            } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                XMPPService.xmpp.login();
            } else {
                resetProfileInfo();
            }
        }
    };

    private void resetProfileInfo() {
        L.debug("reset profile");


        final UserProfile userProfile = new UserProfile();
        userProfile.setUsername(mUname);

       rawImage = BitmapFactory.decodeResource(getResources(), R.drawable.pic_sample_girl);

        profilePic.setImageBitmap(rawImage);

        edtName.setText(userProfile.getUsername());

        new Thread(new Runnable() {
            @Override
            public void run() {


                //userProfile.downloadOffline(getActivity());
                userProfile.loadVCard(XMPPService.xmpp.connection);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        edtName.setText(userProfile.getUsername());
                        String imgDecodableString = Utilz.getDataFrmSharedPref(getActivity().getApplicationContext(), UserProfile.AVATAR_DIR, "");
                        if (imgDecodableString != null && !imgDecodableString.equalsIgnoreCase("")) {
                            // Get the dimensions of the View
                            int targetW = profilePic.getWidth();
                            int targetH = profilePic.getHeight();

                            BmpFactory bmpFactory = new BmpFactory();
                            Bitmap newImage = bmpFactory.getBmpWithTargetWTargetHFrm(targetW, targetH, imgDecodableString);
                            if (newImage != null) rawImage = newImage;

                        } else  {
                            if(userProfile.getAvatarImg() != null){
                                rawImage = userProfile.getAvatarImg();
                            }


                        }

                        profilePic.setImageBitmap(rawImage);


                        if (userProfile.getDescription() != null) {
                            edtDescription.setText(userProfile.getDescription());
                            edtDescription.setSelection(userProfile.getDescription().length());
                        }

                        if (userProfile.getProfession() != null) {
                            edtProfession.setText(userProfile.getProfession());
                            edtProfession.setSelection(userProfile.getProfession().length());
                        }

                        if (userProfile.getUrl0() != null) {
                            edtUrl0.setText(userProfile.getUrl0());
                            edtUrl0.setSelection(userProfile.getUrl0().length());
                        }

                        if (userProfile.getUrl1() != null) {
                            edtUrl1.setText(userProfile.getUrl1());
                            edtUrl1.setSelection(userProfile.getUrl1().length());
                        }

                        if (userProfile.getUrl2() != null) {
                            edtUrl2.setText(userProfile.getUrl2());
                            edtUrl2.setSelection(userProfile.getUrl2().length());
                        }

                        if(pDialog!=null){
                            pDialog.dismiss();
                            pDialog = null;
                        }
                    }
                });


            }
        }).start();


    }

    private void resetProfilePic() {


        if (profilePic != null) {


            Bitmap rawImage = BitmapFactory.decodeResource(getResources(), R.drawable.pic_sample_girl);
            String imgDecodableString = Utilz.getDataFrmSharedPref(getActivity().getApplicationContext(), UserProfile.AVATAR_DIR, "");
            if (imgDecodableString != null && !imgDecodableString.equalsIgnoreCase("")) {
                L.debug("SettingsActivity, imgDecodableString " + imgDecodableString);
                // Get the dimensions of the View
                int targetW = profilePic.getWidth();
                int targetH = profilePic.getHeight();

                BmpFactory bmpFactory = new BmpFactory();
                Bitmap newImage = bmpFactory.getBmpWithTargetWTargetHFrm(targetW, targetH, imgDecodableString);
                if (newImage != null) rawImage = newImage;

            }

            profilePic.setImageBitmap(rawImage);
        }

    }

    public interface OnShowProfilePicScreenListener {
        public void onShowProfilePicScreen();
    }

}
