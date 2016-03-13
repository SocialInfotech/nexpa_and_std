package com.lpoezy.nexpa.chatservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.objects.OfUser;
import com.lpoezy.nexpa.utility.L;

import java.io.IOException;



public class RegistrationIntentService extends IntentService {


	public RegistrationIntentService() {
		super("RegistrationIntentService");
	}
	
	public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_TOKEN = "gcmToken";
    
    private void sendRegistrationToServer(String token) {
        // send network request
        OfUser ofUser = new OfUser();
        ofUser.setGcmRegistrationId(token);

        ofUser.updateGcmIdOnline(this);


        // if registration sent was successful, store a boolean that indicates whether the generated token has been sent to server
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
     }

	@Override
	protected void onHandleIntent(Intent intent) {
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Make a call to Instance API
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_defaultSenderId);
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            L.debug("GCM Registration Token: " + token);
            
            // save token
            sharedPreferences.edit().putString(GCM_TOKEN, token).apply();
            
            // pass along this data
            sendRegistrationToServer(token);
        } catch (IOException e) {
            L.error("" + e.getMessage());
            
         // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
		
	}
}
