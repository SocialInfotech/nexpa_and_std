package com.lpoezy.nexpa.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class UserProfile {

    public static final String AVATAR_DIR = "AVATAR_DIR";

    private String avatarDir;
    private Bitmap avatarImg;
    private String username;
    private String description;
    private String profession;
    private String url0;
    private String url1;
    private String url2;

    public UserProfile() {
    }

    public UserProfile(String username, String description, String profession, String url0, String url1, String url2, String avatarDir) {

        this.username = username;
        this.description = description;
        this.profession = profession;
        this.url0 = url0;
        this.url1 = url1;
        this.url2 = url2;
        this.avatarDir = avatarDir;
        //this.dateUpdated = dateUpdated;
    }

    public void loadVCard(XMPPConnection connection) {
        L.debug("loading card of "+this.username);

        VCardManager vCardManager = VCardManager.getInstanceFor(connection);

        boolean isSupported = false;
        try {
            isSupported = vCardManager.isSupported(connection.getUser());
        } catch (SmackException.NoResponseException e) {
            L.error("loadVCard: "+e.getMessage());
        } catch (XMPPException.XMPPErrorException e) {
            L.error("loadVCard: "+e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            L.error("loadVCard: "+e.getMessage());
        } catch(IllegalArgumentException e){
            L.error("loadVCard: "+e.getMessage());
        }

        if (isSupported) {

            ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

            VCard loadCard = null;

            try {

                loadCard = vCardManager.loadVCard(this.username + "@" + XMPPService.DOMAIN);

            } catch (SmackException.NoResponseException e) {
                L.error("loadVCard: " + e.getMessage());
            } catch (XMPPException.XMPPErrorException e) {
                L.error("loadVCard: " + e.getMessage());
            } catch (SmackException.NotConnectedException e) {
                L.error("loadVCard: " + e.getMessage());
            }



            if (loadCard != null) {

                this.username = loadCard.getFirstName();
                this.description = loadCard.getField("description");
                this.profession = loadCard.getField("profession");
                this.url0 = loadCard.getField("url0");
                this.url1 = loadCard.getField("url1");
                this.url2 = loadCard.getField("url2");

                byte[] avatarByteArr = loadCard.getAvatar();
                if(avatarByteArr!=null) {
                    this.avatarImg = BitmapFactory.decodeByteArray(avatarByteArr, 0, avatarByteArr.length);
                }
            }


//
        } else {
            L.error("vCard not supported");
        }



    }


    public void saveVCard(XMPPConnection connection) {


        VCard saveCard = new VCard();
        saveCard.setFirstName(this.username);

        saveCard.setField("description", this.description);
        saveCard.setField("profession", this.profession);
        saveCard.setField("url0", this.url0);
        saveCard.setField("url1", this.url1);
        saveCard.setField("url2", this.url2);


        BmpFactory bmpFactory = new BmpFactory();
        final int MAX_SIZE = 175;
        Bitmap bmp = bmpFactory.getBmpWithTargetWTargetHFrm(MAX_SIZE, MAX_SIZE, avatarDir);

        if(bmp!=null){

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.PNG, 90, stream); // compress to which format you want.

            byte[] byte_arr = stream.toByteArray();
            //final String imageStr = Base64.encodeToString(byte_arr);//Base64.encodeBytes(byte_arr)

            saveCard.setAvatar(byte_arr, "image/png");

            saveCard.setJabberId(this.username + "@" + XMPPService.DOMAIN);
        }


        VCardManager vCardManager = VCardManager.getInstanceFor(connection);

        boolean isSupported = false;
        try {
            isSupported = vCardManager.isSupported(connection.getUser());
        } catch (SmackException.NoResponseException e) {
            L.error("saveVCard: "+e.getMessage());
        } catch (XMPPException.XMPPErrorException e) {
            L.error("saveVCard: "+e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            L.error("saveVCard: "+e.getMessage());
        }

        if (isSupported) {

            try {
                vCardManager.saveVCard(saveCard);

                L.debug("saving profile");
            } catch (SmackException.NoResponseException e) {
                L.error(e.getMessage());
            } catch (XMPPException.XMPPErrorException e) {
                L.error(e.getMessage());
            } catch (SmackException.NotConnectedException e) {
                L.error(e.getMessage());
            }
        } else {
            L.error("vCard not supported");
        }
    }

    public interface OnCArdReadyListener{

        public void onCArdReady();

    }

//	public UserProfile(String username, String description, String profession, String url0, String url1,
//			String url2, String dateUpdated, boolean isUnsyncedOnline) {
//
//		this.username = username;
//		this.description = description;
//		this.profession = profession;
//		this.url0 = url0;
//		this.url1 = url1;
//		this.url2 = url2;
//		this.dateUpdated = dateUpdated;
//		this.isSyncedOnline = isUnsyncedOnline;
//	}

//	public long getId() {
//		return id;
//	}
//
//	public void setId(long id) {
//		this.id = id;
//	}


    public Bitmap getAvatarImg() {
        return avatarImg;
    }

    public void setAvatarImg(Bitmap avatarImg) {
        this.avatarImg = avatarImg;
    }

    public String getAvatarDir() {
        return avatarDir;
    }

    public void setAvatarDir(String avatarDir) {
        this.avatarDir = avatarDir;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getUrl0() {
        return url0;
    }

    public void setUrl0(String url0) {
        this.url0 = url0;
    }

    public String getUrl1() {
        return url1;
    }

    public void setUrl1(String url1) {
        this.url1 = url1;
    }

    public String getUrl2() {
        return url2;
    }

    public void setUrl2(String url2) {
        this.url2 = url2;
    }

//	public String getDateUpdated() {
//		return dateUpdated;
//	}
//
//	public void setDateUpdated(String dateUpdated) {
//		this.dateUpdated = dateUpdated;
//	}

//	public boolean isSyncedOnline() {
//		return isSyncedOnline;
//	}
//
//	public void setSyncedOnline(boolean isSyncedOnline) {
//		this.isSyncedOnline = isSyncedOnline;
//	}

    public void saveOffline(Context context) {

        SQLiteHandler db = new SQLiteHandler(context);

        db.openToWrite();
        db.saveUserProfile(username, description, profession, url0, url1, url2, avatarDir);
        db.close();
    }

    public boolean saveOnline(Context context) {
//		L.debug("start updating user info online");
//		SQLiteHandler db = new SQLiteHandler(context);
//		db.openToRead();
//		setId(Long.parseLong(db.getLoggedInID()));
//
//		HashMap<String, String> postDataParams = new HashMap<String, String>();
//
//		postDataParams.put("tag", "profile_update");
//		postDataParams.put("user_id", Long.toString(this.id));
//		postDataParams.put("username", this.username);
//		postDataParams.put("description", this.description);
//		postDataParams.put("title", this.profession);
//		postDataParams.put("url0", this.url0);
//		postDataParams.put("url1", this.url1);
//		postDataParams.put("url2", this.url2);
//		postDataParams.put("date_updated", this.dateUpdated);
////{"tag":"profile_update","error":false,"msg":" profile update sucess!"}
//		final String spec = AppConfig.URL_USER_PROFILES;
//		String webPage = HttpUtilz.makeRequest(spec, postDataParams);
//
//		L.debug("UserProfile, webPage: " + webPage);
//
//		db.close();
//
//		boolean isOk = false;
//		try {
//			JSONObject result = new JSONObject(webPage);
//			L.debug("updating user info online complete");
//			if(!result.getBoolean("error"))return true;
//
//
//		} catch (JSONException e) {
//			L.error(""+e);
//		}

        return false;
    }

    public boolean downloadOnline() {

//		HashMap<String, String> postDataParams = new HashMap<String, String>();
//		postDataParams.put("tag", "profile_download");
//		postDataParams.put("user_id", Long.toString(this.id));
//
//		final String spec = AppConfig.URL_USER_PROFILES;
//		String webPage = HttpUtilz.makeRequest(spec, postDataParams);
//
//		L.debug("UserProfile, downloadOnline, webPage: " + webPage);
//
//		try {
//			JSONObject jResult = new JSONObject(webPage);
//			// org.json.JSONException: Value {"id":"8","title":"mobile app
//			// developer","username":"h","url1":"www.lpoezy.com","url2":"www.lpoezy.com","description":"gghuyyrt","date_updated":"2015-12-08
//			// 14:06:41","url0":"www.lpoezy.com"} at user_profiles of type
//			// org.json.JSONObject cannot be converted to JSONArray
//			if (!jResult.getBoolean("error")) {
//
//				JSONObject jProfile = jResult.getJSONObject("user_profiles");
//				this.id = Long.parseLong(jProfile.getString("id"));
//				this.username = jProfile.getString("username");
//				this.description = jProfile.getString("description");
//				this.profession = jProfile.getString("title");
//				this.url0 = jProfile.getString("url0");
//				this.url1 = jProfile.getString("url1");
//				this.url2 = jProfile.getString("url2");
//				this.dateUpdated = jProfile.getString("date_updated");
//
//				return true;
//			}
//
//		} catch (JSONException e) {
//			L.error("" + e);
//		}

        return false;
    }

    public void downloadMyUnsyncedDetailsOffline(Context context) {
//		SQLiteHandler db = new SQLiteHandler(context);
//		db.openToRead();
//
//
//		Map<String, String> map = db.downloadMyUnsyncedDetails();
//
//		if (map != null) {
//			this.id = Long.parseLong(map.get(SQLiteHandler.USER_PROFILE_USER_ID));
//			this.username = map.get(SQLiteHandler.USER_PROFILE_USERNAME);
//			this.description = map.get(SQLiteHandler.USER_PROFILE_DESCRIPTION);
//			this.profession = map.get(SQLiteHandler.USER_PROFILE_PROFESSION);
//			this.url0 = map.get(SQLiteHandler.USER_PROFILE_URL0);
//			this.url1 = map.get(SQLiteHandler.USER_PROFILE_URL1);
//			this.url2 = map.get(SQLiteHandler.USER_PROFILE_URL2);
//			this.dateUpdated = map.get(SQLiteHandler.USER_PROFILE_DATE_UPDATED);
//			this.isSyncedOnline = StringFormattingUtils.getBoolean(map.get(SQLiteHandler.USER_PROFILE_IS_SYNCED_ONLINE));
//
//
//		}
//
//		db.close();

    }

    public void downloadOffline(Context context) {
        SQLiteHandler db = new SQLiteHandler(context);
        db.openToRead();
        Map<String, String> map = db.downloadUserProfile(this.username);
        // L.debug("UserProfile, downloadOffline: "+map.size());
        if (map != null) {

            this.username = map.get(SQLiteHandler.USER_PROFILE_USERNAME);
            this.description = map.get(SQLiteHandler.USER_PROFILE_DESCRIPTION);
            this.profession = map.get(SQLiteHandler.USER_PROFILE_PROFESSION);
            this.url0 = map.get(SQLiteHandler.USER_PROFILE_URL0);
            this.url1 = map.get(SQLiteHandler.USER_PROFILE_URL1);
            this.url2 = map.get(SQLiteHandler.USER_PROFILE_URL2);
            this.avatarDir = map.get(SQLiteHandler.USER_PROFILE_AVATAR_DIR);
        }

        db.close();

    }

    public static List<UserProfile> downloadOnlineWithIds(String ids) {

//		HashMap<String, String> postDataParams = new HashMap<String, String>();
//		postDataParams.put("tag", "profile_download_with_ids");
//		postDataParams.put("user_ids", ids);
//
//		final String spec = AppConfig.URL_USER_PROFILES;
//		String webPage = HttpUtilz.makeRequest(spec, postDataParams);
//
//		L.debug("UserProfile, downloadOnlineWithIds, webPage: " + webPage);
//
//		try {
//			JSONObject jResult = new JSONObject(webPage);
//			// org.json.JSONException: Value {"id":"8","title":"mobile app
//			// developer","username":"h","url1":"www.lpoezy.com","url2":"www.lpoezy.com","description":"gghuyyrt","date_updated":"2015-12-08
//			// 14:06:41","url0":"www.lpoezy.com"} at user_profiles of type
//			// org.json.JSONObject cannot be converted to JSONArray
//			if (!jResult.getBoolean("error")) {
//
//				JSONArray jProfiles = jResult.getJSONArray("user_profiles");
//				List<UserProfile> profiles = new ArrayList<UserProfile>();
//				for (int i = 0; i < jProfiles.length(); i++) {
//
//					JSONObject jProfile = jProfiles.getJSONObject(i);
//
//					long id = Long.parseLong(jProfile.getString("id"));
//					String username = jProfile.getString("username");
//					String description = jProfile.getString("description");
//					String profession = jProfile.getString("title");
//					String url0 = jProfile.getString("url0");
//					String url1 = jProfile.getString("url1");
//					String url2 = jProfile.getString("url2");
//					String dateUpdated = jProfile.getString("date_updated");
//
//					UserProfile prof = new UserProfile(id, username, description, profession, url0, url1, url2,
//							dateUpdated, true);
//
//					profiles.add(prof);
//				}
//
//				return profiles;
//			}
//
//		} catch (JSONException e) {
//			L.error("" + e);
//		}

        return null;

    }


}
