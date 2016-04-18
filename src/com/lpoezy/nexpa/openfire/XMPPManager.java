package com.lpoezy.nexpa.openfire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.activities.ChatActivity;
import com.lpoezy.nexpa.activities.ChatHistoryActivity;
import com.lpoezy.nexpa.activities.CommentsActivity;
import com.lpoezy.nexpa.activities.CommentsFragment;
import com.lpoezy.nexpa.activities.GroupChatHomeActivity;
import com.lpoezy.nexpa.activities.TabHostActivity;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Announcement;
import com.lpoezy.nexpa.objects.BroadcastComment;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.MessageResultElement;
import com.lpoezy.nexpa.objects.NewMessage;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.OnRetrieveMessageArchiveListener;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMPPManager {

    public static boolean connected = false;
    //private final XMPPService.OnProcessMessage processMessageCallback;

    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static AbstractXMPPConnection connection;
    public static String loginUser;
    public static String passwordUser;
    Gson gson;
    Context context;
    public static XMPPManager instance = null;
    public static boolean instanceCreated = false;
    public List<OnExecutePendingTaskListener> pendingTasks;


    public XMPPManager(Context context) {

        String uname, password;
        SQLiteHandler db = new SQLiteHandler(context);
        db.openToRead();


        //this.serverAddress = serverAdress;
        this.loginUser = db.getUsername();
        this.passwordUser = db.getPlainPassword();
        this.serverAddress = XMPPService.DOMAIN;
        this.context = context;
        //this.processMessageCallback = processMessageCallback;
        init();
        db.close();
    }

    public static XMPPManager getInstance(Context context) {

        if (instance == null) {
            instance = new XMPPManager(context);
            instanceCreated = true;
        }
        return instance;

    }

    public org.jivesoftware.smack.chat.Chat Mychat;

    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;

    String text = "";
    String mMessage = "", mReceiver = "";

    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (ClassNotFoundException ex) {
            // problem loading reconnection manager
        }
    }

    public void init() {
        gson = new Gson();
        mMessageListener = new MMessageListener(context);
        mChatManagerListener = new ChatManagerListenerImpl();
        pendingTasks = new ArrayList<OnExecutePendingTaskListener>();
        initialiseConnection();

    }

    @SuppressWarnings("deprecation")
    private void initialiseConnection() {
        L.debug("xmpp, initialiseConnection");
        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        config.setHost(serverAddress);
        config.setPort(5222);

        config.setDebuggerEnabled(true);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        connection = new XMPPTCPConnection(config.build());
        ReconnectionManager connManager = ReconnectionManager.getInstanceFor(connection);
        connManager.disableAutomaticReconnection();

//        connManager.enableAutomaticReconnection();
//        connManager.setFixedDelay(5);
//        connManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.FIXED_DELAY);
        //connManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
        //L.debug("isAutomaticReconnectEnabled: " + ReconnectionManager.getInstanceFor(connection).isAutomaticReconnectEnabled());

        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
    }

    public void disconnect() throws NotConnectedException, XMPPErrorException, NoResponseException {

        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setStatus("I'm unavailable");
        connection.sendPacket(presence);

        connection.disconnect();
        instance = null;
        connected = false;
        chat_created = false;
        loggedin = false;

        L.debug("xmpp, Disconnected");


    }

    public void connect(final String caller) {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {

                if (connection.isConnected())
                    return false;
                isconnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context,
                                    caller + "=>connecting....",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                L.debug("Connect() Function " + caller + "=>connecting....");

                try {

                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    connected = true;


                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "IOException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    L.error("(" + caller + "), IOException: " + e.getMessage());
                } catch (SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "(" + caller + ")" + "SMACKException: ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    L.error("(" + caller + "), SMACKException: "
                            + e.getMessage());
                } catch (XMPPException e) {
                    if (isToasted)

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "XMPPException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    L.error("connect(" + caller + ")" +
                            "XMPPException: " + e.getMessage());

                }


                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void register(String uname, String password, String email)
            throws NoResponseException, XMPPErrorException,
            NotConnectedException {

        AccountManager accountManager = AccountManager.getInstance(connection);
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", uname);
        map.put("name", uname);
        map.put("password", password);
        map.put("email", email);
        accountManager.deleteAccount();
        accountManager.createAccount(uname, password, map);

        L.debug("REGISTER, New user created successfully." + uname + ", "
                + password + ", " + email);

    }

    public void login() {


        try {
            connection.login(loginUser, passwordUser);

            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("I'm available");
            connection.sendStanza(presence);


            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");


        } catch (XMPPException e) {
            L.error(e.getMessage());
        } catch (SmackException e) {
            L.error(e.getMessage());
        } catch (IOException e) {
            L.error(e.getMessage());
        }


    }

    public void notifyConnectedToOPenfireListeners(XMPPConnection connection) {
        for (OnConnectedToOPenfireListener ob : mOnConnectedToOPenfireListeners) {
            L.debug("notifyConnectedToOPenfireListeners...");
            ob.onConnectedToOpenfire(connection);

        }
    }

    ChatActivity.OnProcessMessage mProcessMessageListener;

    public void addOnProcessMessageListener(ChatActivity.OnProcessMessage processMessageListener) {
        mProcessMessageListener = processMessageListener;
    }

    private List<GroupChatHomeActivity.OnUpdateUIListener> mUpdateBroadcastUIListeners = new ArrayList<GroupChatHomeActivity.OnUpdateUIListener>();

    public void registerUpdateBroadcastUIListener(GroupChatHomeActivity.OnUpdateUIListener listener) {
        mUpdateBroadcastUIListeners.add(listener);
    }

    private void notifyUpdateBroadcastListeners() {

        for (GroupChatHomeActivity.OnUpdateUIListener listener : mUpdateBroadcastUIListeners) {
            listener.onUpdateUI();
        }
    }

    public void removeUpdateBroadcastUIListener(GroupChatHomeActivity.OnUpdateUIListener listener) {
        mUpdateBroadcastUIListeners.remove(listener);
    }

    private void notifyUpdateCommentsListeners() {

        for (CommentsFragment.OnUpdateUIListener listener : mUpdateCommentsUIListeners) {
            listener.onUpdateUI();
        }
    }

    List<CommentsFragment.OnUpdateUIListener> mUpdateCommentsUIListeners = new ArrayList<CommentsFragment.OnUpdateUIListener>();

    public void registerUpdateCommentsUIListener(CommentsFragment.OnUpdateUIListener listener) {
        mUpdateCommentsUIListeners.add(listener);
    }

    public void removeUpdateCommentsUIListener(CommentsFragment.OnUpdateUIListener listener) {
        mUpdateCommentsUIListeners.remove(listener);
    }

    public interface OnConnectedToOPenfireListener {
        public void onConnectedToOpenfire(XMPPConnection connection);
    }

    private List<OnConnectedToOPenfireListener> mOnConnectedToOPenfireListeners = new ArrayList<OnConnectedToOPenfireListener>();

    public void removeConnectedToOPenfireListeners(OnConnectedToOPenfireListener observer) {
        mOnConnectedToOPenfireListeners.remove(observer);
    }

    public void addConnectedToOPenfireListeners(OnConnectedToOPenfireListener observer) {

        mOnConnectedToOPenfireListeners.add(observer);

    }

    public void notifyMAMListeners(List<MessageResultElement> msgs, int first, int last, int count) {
        for (OnRetrieveMessageArchiveListener ob : mOnRetrievedMAMListeners) {
            // L.debug("notifyMAMListeners...");
            ob.onRetrieveMessageArchive(msgs, first, last, count);

        }
    }


    private List<OnRetrieveMessageArchiveListener> mOnRetrievedMAMListeners = new ArrayList<OnRetrieveMessageArchiveListener>();

    public void removeMAMListeners(OnRetrieveMessageArchiveListener observer) {
        mOnRetrievedMAMListeners.remove(observer);
    }

    public void addMAMListeners(OnRetrieveMessageArchiveListener observer) {

        mOnRetrievedMAMListeners.add(observer);
    }


    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {


            if (!createdLocally)
                chat.addMessageListener(mMessageListener);

        }

    }

//    public void retrieveListOfCollectionsFrmMsgArchive(final String with) {
//        L.debug("retrieveListOfCollectionsFrmMsgArchive");
//
//        if (connection.isAuthenticated()) {
//            MessageArchiveWithIQ mam = new MessageArchiveWithIQ(with);
//            mam.setType(IQ.Type.set);
//            try {
//                connection.sendStanza(mam);
//            } catch (NotConnectedException e) {
//                L.error("retrieveListOfCollectionsFrmMsgArchive: " + e.getBody());
//
//            }
//
//            final List<MessageResultElement> msgElements = new ArrayList<MessageResultElement>();
//
//            ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0",
//                    new MAMExtensionProvider(
//                            new MessageResultElement.OnParseCompleteListener() {
//
//                                @Override
//                                public void onParseComplete(MessageResultElement msg) {
//
//                                    //L.debug("msgs: "+msgs.size());
//
//                                    msgElements.add(msg);
//                                }
//                            }
//                    ));
//
//            ProviderManager.addExtensionProvider("fin", "urn:xmpp:mam:0",
//                    new MAMFinExtensionProvider(
//                            new MAMFinExtensionProvider.OnParseCompleteListener() {
//
//                                @Override
//                                public void onParseComplete(final int first, final int last, final int count) {
//
//                                    L.debug("msgs: " + msgElements.size() + ", onParseComplete: first: " + first + ", last: " + last + ", count: " + count);
//                                    notifyMAMListeners(msgElements, first, last, count);
//
//                                }
//                            }
//                    ));
//        } else {
//            try {
//                login();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (XMPPException e) {
//                e.printStackTrace();
//            } catch (SmackException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }

    public void sendMessage(ChatMessage chatMessage) {
        String body = gson.toJson(chatMessage);

        if (!chat_created) {
            L.debug("to: " + chatMessage.receiver + "@" + "198.154.106.139");
            Mychat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.receiver + "@"
                            + this.serverAddress,
                    mMessageListener);
            chat_created = true;
        }
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.msgid);
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {

                Mychat.sendMessage(message);

            } else {
                login();
            }
        } catch (NotConnectedException e) {
            L.error("xmpp.SendMessage(), msg Not sent!-Not Connected!");

        } catch (Exception e) {
            L.error("xmpp.SendMessage()-Exception, msg Not sent!"
                    + e.getMessage());
        }

    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection conn) {

            L.debug("xmpp, Connected");
            connected = true;

            if (!conn.isAuthenticated() && conn.getUser() == null) {

                login();
            }


        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });


            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            L.error("xmpp, ConnectionClosedOn Error! " + arg0.getMessage());
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            L.debug("xmpp, Reconnectingin " + arg0);

            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(context, "ReconnectionFailed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            L.error("xmpp, ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;


        }


        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "REConnected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            L.debug("xmpp, ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;


        }

        @Override
        public void authenticated(XMPPConnection conn, boolean arg1) {

            L.debug("xmpp, Authenticated!");
            loggedin = true;

            ChatManager.getInstanceFor(conn).addChatListener(
                    mChatManagerListener);

            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "Connected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });

            for (int i = pendingTasks.size() - 1; i >= 0; i--) {
                pendingTasks.get(i).onExecutePendingTask();
                pendingTasks.remove(i);
            }
        }
    }

    private PendingIntent getNotificationPendingIntent(ChatMessage chat) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.putExtra("username", chat.senderName);
        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

    }

    private void sendNotification(ChatMessage chat) {
        chat.isMine = false;
        int msgCount = NewMessage.getUnReadMsgCountOffline(context);

        String title = "new message";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title).setAutoCancel(true).setContentText(chat.senderName);

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;

        PendingIntent resultPendingIntent = getNotificationPendingIntent(chat);

        // if (Build.VERSION.SDK_INT == 19) {
        // resultPendingIntent.cancel();
        // }

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(AppConfig.MSG_NOTIFICATION_ID, mBuilder.build());

    }


    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {


            L.debug("MyXMPP_MESSAGE_LISTENER, Xmpp message received");

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {

                final ChatMessage chatMessage = gson.fromJson(
                        message.getBody(), ChatMessage.class);


                if (!TabHostActivity.isRunning && !ChatHistoryActivity.isRunning
                        && !ChatActivity.isRunning) {

                    L.debug("sending nottification!");

                    // send notification
                    sendNotification(chatMessage);

                } else {
                    processMessage(chatMessage);
                }


            } else {
                if (GroupChatHomeActivity.isRunning) {

                    L.debug("broadcast: " + message.toXML());

                    parseReceivedBroadcast(message);

                } else if (CommentsActivity.isRunning) {
                    L.debug("comments: " + message.toXML());

                    parseReceivedComment(message);
                }
            }
        }

        private void parseReceivedComment(Message message) {
            String comment = null;
            String itemId = null;
            XmlPullParserFactory factory = null;
            try {
                factory = XmlPullParserFactory.newInstance();


                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(message.toXML().toString()));
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        // L.debug("Start document");
                    } else if (eventType == XmlPullParser.START_TAG) {
                        //L.debug("Start tag " + xpp.getName());
                        switch (xpp.getName()) {
                            case "item":
                                itemId = xpp.getAttributeValue("", "id");
                                break;
                            case "comment":
                                comment = xpp.nextText();
                                break;
                        }


                    } else if (eventType == XmlPullParser.END_TAG) {
                        // L.debug("End tag " + xpp.getName());
                    } else if (eventType == XmlPullParser.TEXT) {
                        //L.debug("Texxt " + xpp.getText());
                    }
                    eventType = xpp.next();
                }

                //L.debug("End document");
            } catch (XmlPullParserException e) {
                //L.error(e.getMessage());
            } catch (IOException e) {
                L.error(e.getMessage());
            }

            L.debug("itemId: " + itemId + ", comment: " + comment);

            if (comment != null && !comment.isEmpty()) {

                BroadcastComment bc = gson.fromJson(comment, BroadcastComment.class);

                SQLiteHandler db = new SQLiteHandler(context);
                db.openToWrite();

                if (!bc.getFrom().equals(db.getUsername())) {
                    bc.setIsMine(false);
                }

                db.close();

            }

            notifyUpdateCommentsListeners();
        }

        private void parseReceivedBroadcast(Message message) {

            String broadcast = null;
            String itemId = null;
            XmlPullParserFactory factory = null;
            try {
                factory = XmlPullParserFactory.newInstance();


                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(message.toXML().toString()));
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        //L.debug("Start document");
                    } else if (eventType == XmlPullParser.START_TAG) {
                        //L.debug("Start tag " + xpp.getName());
                        switch (xpp.getName()) {
                            case "item":
                                itemId = xpp.getAttributeValue("", "id");
                                break;
                            case "broadcast":
                                broadcast = xpp.nextText();
                                break;
                        }

//                                if(xpp.getName().equals("broadcast")){
//                                    broadcast = xpp.nextText();
//                                }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        //L.debug("End tag " + xpp.getName());
                    } else if (eventType == XmlPullParser.TEXT) {
                        //L.debug("Teb xt " + xpp.getText());
                    }
                    eventType = xpp.next();
                }

                //L.debug("End document");
            } catch (XmlPullParserException e) {
                L.error(e.getMessage());
            } catch (IOException e) {
                L.error(e.getMessage());
            }
            //L.debug("broadcast: "+broadcast);
            if (broadcast != null && !broadcast.isEmpty()) {

                SQLiteHandler db = new SQLiteHandler(context);
                db.openToWrite();
                try {
                    Announcement ann = gson.fromJson(broadcast, Announcement.class);
                    ann.setItemId(itemId);

                    if (!ann.getFrom().equals(db.getUsername())) {
                        ann.setIsMine(false);
                    }
                    ann.saveOffline(context);
                    //GroupChatHomeActivity.addNewAnnouncement(ann);
                    //send broadcast here
                    db.close();

                    notifyUpdateBroadcastListeners();
                } catch (Exception e) {
                    L.error(e.getMessage());
                }

            }
        }

        private void processMessage(final ChatMessage chatMessage) {
            chatMessage.isMine = false;
            mProcessMessageListener.onProcessMessage(chatMessage);

        }

    }


}
