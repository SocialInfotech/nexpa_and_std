package com.lpoezy.nexpa.openfire;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lpoezy.nexpa.activities.ChatActivity;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.objects.MAMExtensionProvider;
import com.lpoezy.nexpa.objects.MessageArchiveAllIQ;
import com.lpoezy.nexpa.objects.MessageArchiveWithIQ;
import com.lpoezy.nexpa.objects.MessageElement;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMPPManager {

    public static boolean connected = false;
    private final XMPPService.OnProcessMessage processMessageCallback;
    private final XMPPService.OnConnectedToOPenfireListener connectedToOperfire;
    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static AbstractXMPPConnection connection;
    public static String loginUser;
    public static String passwordUser;
    Gson gson;
    XMPPService context;
    public static XMPPManager instance = null;
    public static boolean instanceCreated = false;

    public XMPPManager(XMPPService context, String serverAdress,
                       String logiUser, String passwordser, XMPPService.OnProcessMessage processMessageCallback, XMPPService.OnConnectedToOPenfireListener connectedToOperfire) {
        this.serverAddress = serverAdress;
        this.loginUser = logiUser;
        this.passwordUser = passwordser;
        this.context = context;
        this.processMessageCallback = processMessageCallback;
        this.connectedToOperfire = connectedToOperfire;
        init();

    }

    public static XMPPManager getInstance(XMPPService context, String server,
                                          String user, String pass, XMPPService.OnProcessMessage processMessageCallback, XMPPService.OnConnectedToOPenfireListener connectedToOperfire) {

        if (instance == null) {
            instance = new XMPPManager(context, server, user, pass, processMessageCallback, connectedToOperfire);
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
                    Log.e("connect(" + caller + ")",
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
            connection.sendPacket(presence);


            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");

        } catch (XMPPException | SmackException | IOException e) {
            L.error("xxx: " + e.getMessage());
        } catch (Exception e) {
            L.error("yyy: " + e.getMessage());
        }


    }


    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {


            if (!createdLocally)
                chat.addMessageListener(mMessageListener);

        }

    }

    public void retrieveCollectionFrmMsgArchive(final String with, final ChatActivity.OnRetrieveMessageArchiveListener callback) {

        if (connection.isAuthenticated()) {
            L.debug("==========retrieveCollectionFrmMsgArchive========================");

            MessageArchiveWithIQ collectionIq = new MessageArchiveWithIQ(with);
            collectionIq.setType(IQ.Type.set);
            try {


                connection.sendStanza(collectionIq);


//                MAMExtensionProviderWith prov = new MAMExtensionProviderWith();
//                prov.addOnParseListener(new MAMExtensionProviderWith.OnTest() {
//                    @Override
//                    public void onTest() {
//                        L.debug("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//                    }
//                });
//                prov.addOnParseListener(new MessageElement.OnParseCompleteListener() {
//                    @Override
//                    public void onParseComplete(List<MessageElement> msgs, int first, int last, int count) {
//                        L.debug("onParseComplete: first: " + first + ", last: " + last + ", count: " + count);
//                        List<ChatMessage> conversation = new ArrayList<ChatMessage>();
//                        if (msgs != null && !msgs.isEmpty()) {
//
//
//                            SQLiteHandler db = new SQLiteHandler(context);
//                            db.openToRead();
//                            for (int i = msgs.size() - 1; i >= 0; i--) {
//                                ChatMessage chat = gson.fromJson(msgs.get(i).getBody(), ChatMessage.class);
//
//                                if (chat.receiver.equals(db.getUsername())) {
//                                    chat.isMine = false;
//                                }
//
//                                conversation.add(chat);
//                            }
//
//                            db.close();
//                        }
//
//                        if (callback != null) {
//                            callback.onRetrieveMessageArchive(conversation);
//                        }
//
//
//                    }
//                });

                //ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0", prov);

            } catch (NotConnectedException e) {
                L.error("retrieveCollectionFrmMsgArchive: " + e.getMessage());
            }
        } else {
            login();
        }
    }


    public void retrieveListOfCollectionsFrmMsgArchive(final String with, final XMPPService.OnUpdateScreenListener callback0, final ChatActivity.OnRetrieveMessageArchiveListener callback1) {

        if (connection.isAuthenticated()) {
            L.debug("==========retrieveListOfCollectionsFrmMsgArchive========================"+with);

            IQ mam = null;
            if (with == null) {
                mam = new MessageArchiveAllIQ();
            } else {
                mam = new MessageArchiveWithIQ(with);
            }
            mam.setType(IQ.Type.set);

            try {
                connection.sendStanza(mam);

                MAMExtensionProvider prov = new MAMExtensionProvider(with,
                        new MessageElement.OnParseCompleteListener() {

                    @Override
                    public void onParseComplete(List<MessageElement> msgs, int first, int last, int count, String with) {


                        L.debug("onParseComplete: first: " + first + ", last: " + last + ", count: " + count+", with: "+with);
                        List<ChatMessage> conversation = new ArrayList<ChatMessage>();
                        if (msgs != null && !msgs.isEmpty()) {


                            SQLiteHandler db = new SQLiteHandler(context);
                            db.openToWrite();
                            if (with == null) {
                                //save messages to offline db


                                db.deleteMsgArchive();
                                db.saveMsgArchive(msgs);

                            } else {

                                for (int i = msgs.size() - 1; i >= 0; i--) {
                                    ChatMessage chat = gson.fromJson(msgs.get(i).getBody(), ChatMessage.class);

                                    if (chat.receiver.equals(db.getUsername())) {
                                        chat.isMine = false;
                                    }

                                    conversation.add(chat);
                                }

                            }
                            db.close();
                        }

                        if (callback0 != null) {
                            callback0.onUpdateScreen();

                        }

                        if (callback1 != null) {
                            callback1.onRetrieveMessageArchive(conversation);
                        }
                    }
                });


                ProviderManager.removeExtensionProvider("result", "urn:xmpp:mam:0");
                ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0", prov);


            } catch (NotConnectedException e) {
                L.error("retrieveListOfCollectionsFrmMsgArchive: " + e.getMessage());
            }
            //ProviderManager.addExtensionProvider("result", "urn:xmpp:mam:0", new MAMExtensionProvider());
        } else {

            login();

        }


    }

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
        public void connected(final XMPPConnection connection) {


            connected = true;

            if (!connection.isAuthenticated()) {

                login();
            }

            connectedToOperfire.onConnectedToOpenfire(connection);
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
            Log.d("xmpp", "ConnectionClosedOn Error!");
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
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            L.debug("xmpp, Authenticated! " + connection.getUser());
            loggedin = true;

            ChatManager.getInstanceFor(connection).addChatListener(
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
        }
    }

    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            L.debug("MyXMPP_MESSAGE_LISTENER, Xmpp message received: '"
                    + message);

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {

                final ChatMessage chatMessage = gson.fromJson(
                        message.getBody(), ChatMessage.class);

                processMessage(chatMessage);
            }
        }

        private void processMessage(final ChatMessage chatMessage) {
            chatMessage.isMine = false;

            processMessageCallback.onProcessMessage(chatMessage);


        }

    }

}
