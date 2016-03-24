package com.lpoezy.nexpa.objects;

import android.content.Context;

import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.HttpUtilz;
import com.lpoezy.nexpa.utility.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by ldonios on 3/13/2016.
 */
public class OfUser {

    private String username;
    private String plainPassword;
    private String encryptedPassword;
    private String name;
    private String email;
    private long creationDate;
    private long modificationDate;
    private String gcmRegistrationId;

    public OfUser() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getGcmRegistrationId() {
        return gcmRegistrationId;
    }

    public void setGcmRegistrationId(String gcmRegistrationId) {
        this.gcmRegistrationId = gcmRegistrationId;
    }

    public boolean saveOffline(Context c){

        if(name==null ||name.equals("null")|| email==null || email.equals("null"))return false;

        SQLiteHandler db = new SQLiteHandler(c);
        db.openToWrite();
        boolean success = db.saveOfuser(username, plainPassword,  encryptedPassword, name, email, creationDate, modificationDate)!=null?true:false;
        db.close();

        return success;
    }

    public boolean downloadOnline()  {

        HashMap<String, String> postDataParams = new HashMap<String, String>();

        postDataParams.put("tag", "get_ofuser_by_uname");
        postDataParams.put("username", this.username);


        final String spec = AppConfig.URL_OFUSER;
        String webPage = HttpUtilz.makeRequest(spec, postDataParams);

        L.debug("downloadOfUserOnline: " + webPage);
        JSONObject result = null;
        try {
            result = new JSONObject(webPage);

            if(!result.getBoolean("error")){
                JSONObject user = result.getJSONObject("user");
                this.encryptedPassword = user.getString("encryptedPassword");
                this.name = user.getString("name");
                this.email = user.getString("email");
                if(!user.getString("creationDate").equals("null"))this.creationDate = Long.parseLong(user.getString("creationDate"));
                if(!user.getString("modificationDate").equals("null"))this.modificationDate = Long.parseLong(user.getString("modificationDate"));

                return true;
            }



        } catch (JSONException e) {
           L.error(e.getMessage());
        }


        return false;

    }

    public void updateGcmIdOnline(Context c) {

        HashMap<String, String> postDataParams = new HashMap<String, String>();

        SQLiteHandler db = new SQLiteHandler(c);
        db.openToRead();


        postDataParams.put("tag", "update_gcm_id");
        postDataParams.put("username", db.getUsername());
        postDataParams.put("gcmRegistrationId", this.gcmRegistrationId);


        final String spec = AppConfig.URL_OFUSER;
        String webPage = HttpUtilz.makeRequest(spec, postDataParams);

        L.debug("updateGcmIdOnline: "+webPage);

        db.close();
    }

}
