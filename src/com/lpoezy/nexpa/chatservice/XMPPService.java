package com.lpoezy.nexpa.chatservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.activities.ChatActivity;
import com.lpoezy.nexpa.activities.ChatHistoryActivity;
import com.lpoezy.nexpa.activities.TabHostActivity;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.NewMessage;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.HttpUtilz;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMPPService extends Service {
    public static final String DOMAIN = "198.154.106.139";// vps.gigapros.com
    // ||
    // 198.154.106.139
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "zbwzdagoW6SKk";// 9db9b6749ffbf61e19aea4358a11d837
    // | zbwzdagoW6SKk
    public static ConnectivityManager cm;
    public static XMPPManager xmpp;
    public static boolean ServerchatCreated = false;
    private static boolean isRunning = false;
    String text = "";

    @Override
    public IBinder onBind(final Intent intent) {
        return new LocalBinder<XMPPService>(XMPPService.this);
    }

    public Chat chat;


    private PendingIntent getNotificationPendingIntent(Correspondent correspondent) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ChatActivity.class);
        resultIntent.putExtra("userid", correspondent.getId());
        resultIntent.putExtra("username", correspondent.getUsername());
        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

    }

    private void sendNotification(Correspondent correspondent) {

        int msgCount = NewMessage.getUnReadMsgCountOffline(getApplicationContext());

        String title = (msgCount > 1) ? " new chtMessages" : " new message";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(msgCount + title).setAutoCancel(true).setContentText(correspondent.getUsername());

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;

        PendingIntent resultPendingIntent = getNotificationPendingIntent(correspondent);

        // if (Build.VERSION.SDK_INT == 19) {
        // resultPendingIntent.cancel();
        // }

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(AppConfig.MSG_NOTIFICATION_ID, mBuilder.build());

    }

    private List<OnConnectedToOPenfireListener> connectedToOperfireListeners = new ArrayList<OnConnectedToOPenfireListener>();

    public void addconnectedToOperfireListener(OnConnectedToOPenfireListener observer) {

        connectedToOperfireListeners.add(observer);
    }

    private OnConnectedToOPenfireListener connectedToOperfire = new OnConnectedToOPenfireListener() {
        @Override
        public void onConnectedToOpenfire(XMPPConnection connection) {


            for (OnConnectedToOPenfireListener observer : connectedToOperfireListeners) {
                observer.onConnectedToOpenfire(connection);
            }

        }
    };


    private List<OnProcessMessage> chatMessagesListeners = new ArrayList<OnProcessMessage>();

    public void addMessageListener(OnProcessMessage observer) {
        chatMessagesListeners.add(observer);
    }


    private OnProcessMessage processMessageCallback = new OnProcessMessage() {

        @Override
        public void onProcessMessage(ChatMessage chatMessage) {


            if (!TabHostActivity.isRunning && !ChatHistoryActivity.isRunning
                    && !ChatActivity.isRunning) {

                L.debug("sending nottification!");

                // send notification
                //sendNotification(correspondent);

            } else {
                for (OnProcessMessage observer : chatMessagesListeners) {
                    observer.onProcessMessage(chatMessage);
                }
                // send broadcast
//                Intent broadcast = new Intent(AppConfig.ACTION_RECEIVED_MSG);
//                broadcast.putExtra("username", chatMessage.sender);
//                broadcast.putExtra("msg", chatMessage.body);
//
//                L.debug("sending broadcast!");
//                sendBroadcast(broadcast);

            }


        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        L.debug("XMPPService, onCreate");
        String uname, password;
        SQLiteHandler db = new SQLiteHandler(XMPPService.this);
        db.openToRead();
        uname = db.getUsername();
        password = db.getPlainPassword();
        xmpp = XMPPManager.getInstance(XMPPService.this, DOMAIN, uname,
                password, processMessageCallback, connectedToOperfire);
        xmpp.connect("onCreate");


        db.close();
        isRunning = true;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {

        L.debug("xmppservice, onDestroy");
        try {


//            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
//            db.openToWrite();
//
//            db.deleteUsers();
//            db.close();
            xmpp.disconnect();

            xmpp = null;

        } catch (NotConnectedException e) {
            L.error(e.getMessage());
        } catch (XMPPErrorException e) {
            L.error(e.getMessage());
        } catch (NoResponseException e) {
            L.error(e.getMessage());
        }


        isRunning = false;

        super.onDestroy();

    }

    public static boolean isNetworkConnected() {
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public void resetPassword(final String email,
                              final OnUpdateScreenListener callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {

                L.debug("resetPassword " + AppConfig.URL_SEND_EMAIL);
                HashMap<String, String> postDataParams = new HashMap<String, String>();
                postDataParams.put("tag", "reset_password");
                postDataParams.put("email", email);

                final String spec = AppConfig.URL_SEND_EMAIL;
                String webPage = HttpUtilz.makeRequest(spec, postDataParams);
                L.debug("webPage: " + webPage);
                JSONObject result;
                try {
                    result = new JSONObject(webPage);
                    final boolean error = result.getBoolean("error");

                    callback.onUpdateScreen();

                } catch (JSONException e) {
                    L.error("" + e);
                }

            }
        }).start();

    }

    public void sendMessage(ChatMessage msg) {
        xmpp.sendMessage(msg);
    }

    public void register(final String uname, final String email,
                         final String password, final OnUpdateScreenListener callback) {

        new Thread(new Runnable() {

            @Override
            public void run() {


                if (xmpp.connection.isConnected()) {

                    try {
                        xmpp.register(uname, password, email);

                        callback.onUpdateScreen();

                    } catch (NoResponseException | NotConnectedException e) {
                        callback.onResumeScreen("User is not, or no longer, connected.");
                        L.error(e.getMessage());
                    } catch (XMPPErrorException e) {
                        L.error(e.getMessage());
                        callback.onResumeScreen("User Name already exists, please enter another one.");
                    }

                } else {

                    L.error("Not connected to openfire server!!!");
                    callback.onResumeScreen("Not connected to openfire server!!!");

                }

            }
        }).start();

    }


//    public void logout(final OnUpdateScreenListener callback) {
//
//
//        stopSelf();
//
//    }


    public void retrieveListOfCollectionsFrmMsgArchive(final OnUpdateScreenListener callback) {


        xmpp.retrieveListOfCollectionsFrmMsgArchive(null, callback, null);


    }

    public void retrieveCollectionFrmMsgArchive(final String with, final ChatActivity.OnRetrieveMessageArchiveListener callback) {


        xmpp.retrieveListOfCollectionsFrmMsgArchive(with, null, callback);


    }


//    public void login(final String uname, final String password, final OnUpdateScreenListener callback) {
//
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//
//
//                OfUser ofuser = new OfUser();
//                ofuser.setUsername(uname);
//                ofuser.setPlainPassword(password);
//                try {
//                    ofuser.downloadOnline();
//
//                } catch (JSONException e) {
//                    L.error("" + e.getMessage());
//                }
//
//                if (ofuser.saveOffline(getApplicationContext())) {
//
//
//                    if (xmpp.connection.isConnected()) {
//
//                        try {
//                            xmpp.login(uname, password);
//
//                            if (xmpp.connection.isAuthenticated()) {
//
//                                Presence presence = new Presence(Presence.Type.available);
//                                presence.setStatus("I'm available");
//                                xmpp.connection.sendPacket(presence);
//
//                                SessionManager sm = new SessionManager(getApplicationContext());
//                                sm.setLogin(true);
//
////							Time iq = new Time();
////							//iq.setType(IQ.Type.set);
////							iq.setTo("ldonios-pc");
////							xmpp.connection.sendIqWithResponseCallback(iq,
////									new StanzaListener() {
////
////										@Override
////										public void processPacket(Stanza arg0)
////												throws NotConnectedException {
////
////											L.debug("Send IQ with Response, ****** message "
////													+ arg0);
////										}
////									}, new ExceptionCallback() {
////										@Override
////										public void processException(
////												Exception exception) {
////											exception.printStackTrace();
////											L.error("IO archjieve Exception, "
////													+ exception.getMessage());
////										}
////									});
//
//
//                                callback.onUpdateScreen();
//
//                            }
//
//                        } catch (SmackException.AlreadyLoggedInException e) {
//                            callback.onResumeScreen("This user is already logged in, please use another login name.");
//                        } catch (SmackException e) {
//                            L.error("" + e);
//                            callback.onResumeScreen("User is not, or no longer, connected.");
//                        } catch (XMPPException e) {
//                            callback.onResumeScreen("Please register first");
//                        }
//
//                    } else {
//
//                        // L.error("Not conncted to openfire server!!!");
//                        callback.onResumeScreen("Not conncted to openfire server!!!");
//                    }
//
//
//                }
//
//
//            }
//        }).start();
//
//    }

    public interface OnUpdateScreenListener {
        public void onResumeScreen(String errorMsg);

        public void onUpdateScreen();
    }

    public interface OnProcessMessage {
        public void onProcessMessage(ChatMessage chatMessage);
    }

    public interface OnConnectedToOPenfireListener {
        public void onConnectedToOpenfire(XMPPConnection connection);
    }

}
