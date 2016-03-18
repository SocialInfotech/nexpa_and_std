package com.lpoezy.nexpa.objects;

import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.utility.HttpUtilz;
import com.lpoezy.nexpa.utility.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by HP PAVILION on 3/15/2016.
 */
public class Geolocation {

    private String username;
    private double latitude;
    private double longitude;
    private String gpsProvider;
    private long dateCreate;
    private long dateUpdate;
    private double distance;

    public Geolocation(){}

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(long dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGpsProvider() {
        return gpsProvider;
    }

    public void setGpsProvider(String gpsProvider) {
        this.gpsProvider = gpsProvider;
    }

    public long getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(long dateCreate) {
        this.dateCreate = dateCreate;
    }

    public boolean saveOnline(){
        //L.debug("saving geolocation online");
        HashMap<String, String> postDataParams = new HashMap<String, String>();

        postDataParams.put("tag", "update_user_location");
        postDataParams.put("username", this.username);
        postDataParams.put("longitude", String.valueOf(this.longitude));
        postDataParams.put("latitude", String.valueOf(this.latitude));

        final String spec = AppConfig.URL_GEO;
        String webPage = HttpUtilz.makeRequest(spec, postDataParams);

       // L.debug("Geolocation, saveOnline: "+webPage);

        JSONObject res = null;
        try {
            res = new JSONObject(webPage);
            if(!res.getBoolean("error")){
                return true;
            }
        } catch (JSONException e) {
            L.error(e.getMessage());
        }

        return false;
    }
}
