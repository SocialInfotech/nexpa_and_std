package com.lpoezy.nexpa.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.DateUtils.DateFormatz;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.Utilz;

public class EditProfileFragment extends DialogFragment {

    public static final String TAG = "EditProfileFragment";

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
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        resetProfilePic();
        resetProfileInfo();
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

    private void resetProfileInfo() {
        L.debug("reset profile");
        SQLiteHandler db = new SQLiteHandler(getActivity());
        db.openToRead();
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(db.getUsername());
        userProfile.downloadOffline(getActivity());

        edtName.setText(userProfile.getUsername());

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

        db.close();
    }

    private void resetProfilePic() {


        if (profilePic != null) {
            String imgDecodableString = Utilz.getDataFrmSharedPref(getActivity().getApplicationContext(), UserProfile.AVATAR_DIR, "");

//            if (mProfilePicture == null) mProfilePicture = new ProfilePicture();
//            //ProfilePicture pic = new ProfilePicture();
////			pic.setUserId(userId);
////			pic.downloadOffline(getActivity());
//
//            long userId = -1;
//            SQLiteHandler db = new SQLiteHandler(getActivity());
//            db.openToRead();
//            //userId = Long.parseLong(db.getLoggedInID());
//            //mProfilePicture.setUserId(userId);
//            db.close();


//            if (imgDecodableString != null && !imgDecodableString.equalsIgnoreCase("")) {
//                int pos = imgDecodableString.lastIndexOf("/");
//
//                String imgDir = imgDecodableString.substring(0, pos);
//                String imgFile = Uri.parse(imgDecodableString).getLastPathSegment();
//
//                mProfilePicture.setImgDir(imgDir);
//                mProfilePicture.setImgFile(imgFile);
//
//            } else {
//
//                mProfilePicture.downloadOffline(getActivity());
//                imgDecodableString = mProfilePicture.getImgDir() + "/" + mProfilePicture.getImgFile();
//            }


            //ProfilePicture pic = new ProfilePicture(userId, imgDir, imgFile, dateCreated, isSyncedOnline);
            //pic.saveOffline(getActivity());


//			ProfilePicture pic = new ProfilePicture();
//			pic.setUserId(userId);
//			pic.downloadOffline(getActivity());
//
//			
//			String imgDecodableString = pic.getImgDir() + "/" + pic.getImgFile();

            Bitmap rawImage = BitmapFactory.decodeResource(getResources(), R.drawable.pic_sample_girl);

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
