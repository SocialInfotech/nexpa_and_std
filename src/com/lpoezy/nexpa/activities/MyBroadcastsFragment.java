
package com.lpoezy.nexpa.activities;

import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Announcement;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.PubSubBroadcastStreamer;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.BmpFactory;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.L;
import com.novoda.sexp.SimpleEasyXmlParser;
import com.novoda.sexp.Streamer;
import com.novoda.sexp.finder.ElementFinder;
import com.novoda.sexp.finder.ElementFinderFactory;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MyBroadcastsFragment extends Fragment {


    List<Announcement> mAnouncements;
    //private MyBroascastsAdapter adapter;
    private RecyclerView mRvBroadcasts;
    protected String mUsername;


    public static MyBroadcastsFragment newInstance() {
        MyBroadcastsFragment fragment = new MyBroadcastsFragment();

        return fragment;
    }

    public MyBroadcastsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    ParallaxRecyclerAdapter<Announcement> mAdapter;
    private ImageView mImgProfile;
    private TextView mTvJobTitle;
    private TextView mTvUname;
    private TextView mTvUrl0;
    private TextView mTvUrl1;
    private TextView mTvUrl2;

    private BroadcastReceiver mActionUserProfileUpdatedReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //resetProfilePic();
            updateUI();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_broadcasts, container, false);

        mRvBroadcasts = (RecyclerView) v.findViewById(R.id.rv_my_broadcasts);

        //mRvChatHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mAnouncements = new ArrayList<Announcement>();

        //adapter = new MyBroascastsAdapter(getActivity());

        mAdapter = new ParallaxRecyclerAdapter<Announcement>(mAnouncements) {


            @Override
            public void onBindViewHolderImpl(android.support.v7.widget.RecyclerView.ViewHolder viewHolder,
                                             ParallaxRecyclerAdapter<Announcement> adapter, int position) {


                Announcement ann = adapter.getData().get(position);

                ViewHolder vh = (ViewHolder) viewHolder;

                vh.tvBroadMsg.setText(ann.getMessage());
                vh.tvReply.setText("REACHED " + ann.getReach());
                vh.ImgReply.setBackgroundResource(R.drawable.btn_reach);
                vh.tvBroadFrm.setText(mUsername);

                DateUtils du = new DateUtils();
                String dateFormatted = du.getMinAgo(ann.getDate());

                vh.tvDateBroad.setText(dateFormatted);

                vh.tvLocLocal.setVisibility(View.GONE);

                if (ann.getLocLocal() != null && !ann.getLocLocal().isEmpty()) {
                    String strLoc = "near " + ann.getLocLocal();

                    vh.tvLocLocal.setText(strLoc);
                    vh.tvLocLocal.setVisibility(TextView.VISIBLE);
                }

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        0, LayoutParams.WRAP_CONTENT, 1.2f);
                vh.btnReply.setLayoutParams(param);


            }

            @Override
            public android.support.v7.widget.RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, ParallaxRecyclerAdapter<Announcement> adapter, int i) {
                View itemView = getActivity().getLayoutInflater().inflate(R.layout.list_broadcast, parent, false);
                return new ViewHolder(itemView);
            }

            @Override
            public int getItemCountImpl(ParallaxRecyclerAdapter<Announcement> adapter) {

                return mAnouncements.size();
            }


        };

        mRvBroadcasts.setLayoutManager(new LinearLayoutManager(getActivity()));
        View header = getActivity().getLayoutInflater().inflate(R.layout.activity_userprofile_header, mRvBroadcasts, false);
        mAdapter.setParallaxHeader(header, mRvBroadcasts);
        mAdapter.setData(mAnouncements);
        mRvBroadcasts.setAdapter(mAdapter);

        mImgProfile = (ImageView) header.findViewById(R.id.img_profile);


        mTvJobTitle = (TextView) header.findViewById(R.id.tv_job_title);
        mTvUname = (TextView) header.findViewById(R.id.tv_uname);
        mTvUrl0 = (TextView) header.findViewById(R.id.tv_url0);
        mTvUrl1 = (TextView) header.findViewById(R.id.tv_url1);
        mTvUrl2 = (TextView) header.findViewById(R.id.tv_url2);


        ((ImageView) header.findViewById(R.id.img_settings)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                getActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

        ((Button) header.findViewById(R.id.btn_edit_profile)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                //mCallback.onShowEditProfileScreen();
                EditProfileFragment editProfileFrag = EditProfileFragment.newInstance();

                editProfileFrag.show(getFragmentManager().beginTransaction(), EditProfileFragment.TAG);

            }
        });

        return v;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        getActivity().unregisterReceiver(mActionUserProfileUpdatedReceived);
    }

//    class ItemEventCoordinator implements ItemEventListener {
//
//        @Override
//        public void handlePublishedItems(ItemPublishEvent items) {
//            L.debug("Item count: " + items);
//
//
//        }
//    }

    private class RetrieveMyOwnBroadcast implements OnExecutePendingTaskListener{
        @Override
        public void onExecutePendingTask() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!XMPPService.xmpp.connection.isConnected()) {

                        XMPPManager.getInstance(getActivity()).instance = null;

                        XMPPService.xmpp = XMPPManager.getInstance(getActivity());

                        XMPPService.xmpp.connect("onCreate");

                    } else if (!XMPPService.xmpp.connection.isAuthenticated()) {

                        XMPPService.xmpp.login();
                    } else {

                        SQLiteHandler db = new SQLiteHandler(getActivity());
                        db.openToRead();
                        // Create a pubsub manager using an existing XMPPConnection
                        PubSubManager mgr = new PubSubManager(XMPPService.xmpp.connection);

                        LeafNode node = null;
//                        try {
//
//                            //node = mgr.getNode(db.getUsername()+"-broadcast");
//                            ConfigureForm form = new ConfigureForm(DataForm.Type.form.submit);
//                            form.setAccessModel(AccessModel.open);
//                            form.setDeliverPayloads(false);
//                            form.setNotifyRetract(true);
//                            form.setPersistentItems(true);
//                            form.setPublishModel(PublishModel.open);
//
//                            node = (LeafNode) mgr.createNode(db.getUsername() + "-broadcast", form);
//
//                        } catch (SmackException.NoResponseException e) {
//                            L.error(e.getMessage());
//                        } catch (XMPPException.XMPPErrorException e) {
//                            L.error(e.getMessage());
//                        } catch (SmackException.NotConnectedException e) {
//                            L.error(e.getMessage());
//                        }



//                        <iq type='get'
//                        from='francisco@denmark.lit/barracks'
//                        to='pubsub.shakespeare.lit'
//                        id='items2'>
//                        <pubsub xmlns='http://jabber.org/protocol/pubsub'>
//                        <items node='princely_musings' max_items='2'/>
//                        </pubsub>
//                        </iq>

                        //<iq to='pubsub.198.154.106.139' id='bg406-27' type='get'>
                        // <pubsub xmlns='http://jabber.org/protocol/pubsub'>
                        // <items node='kato-broadcast' max_items='25'/>
                        // </pubsub></iq>

                        try {

                            node = mgr.getNode(db.getUsername() + "-broadcast");

                            //node.subscribe(db.getUsername() + "@198.154.106.139");
                            List<Subscription> subs = node.getSubscriptions();
                            String mySubid = null;
                            mUsername = XMPPService.xmpp.connection.getUser().split("@")[0];
                            for(int i = 0; i < subs.size(); i++)
                            {
                                //L.debug(XMPPService.xmpp.connection.getUser()+", "+subs.get(i).getJid()+": "+subs.get(i).getId());
                                if(subs.get(i).getJid().split("@")[0].equals(mUsername)){
                                    mySubid = subs.get(i).getId();
                                    break;
                                }

                            }

                            //<iq to='pubsub.198.154.106.139' id='C8D1I-40' type='get'>
                            // <pubsub xmlns='http://jabber.org/protocol/pubsub'>
                            // <items node='kato-broadcast' subid='2INFMa1a1wnpfqI1lN02OtB547dukzDk4i73BdFC' max_items='100'/>
                            // </pubsub>
                            // </iq>

                            //ee0bfce2-46ac-4914-8e47-0e55f785a6085

                            //L.debug("mySubid: "+mySubid);
                            Collection<PayloadItem<Item>> eventItems = node.getItems(100, mySubid);

                            ElementFinderFactory factory = SimpleEasyXmlParser.getElementFinderFactory();
                            ElementFinder<String> elementFinder = factory.getStringFinder();
                            Streamer<String> broadcastStreamer = new PubSubBroadcastStreamer(elementFinder, "broadcast");

                            List<Announcement>announcements = new  ArrayList<Announcement>();
                            for(Item item : eventItems) {

                                String broadcast = SimpleEasyXmlParser.parse(item.toXML(), broadcastStreamer);
                                //L.debug("broadcast: " + broadcast);

                                Announcement announcement = new Announcement();
                                announcement.setMessage(broadcast);
                            }


                            mAnouncements.clear();
                            mAnouncements.addAll(announcements);

                            L.debug("mAnouncements.size " + mAnouncements.size());

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });


                        } catch (SmackException.NoResponseException e) {
                            L.error(e.getMessage());
                        } catch (XMPPException.XMPPErrorException e) {
                            L.error(e.getMessage());
                        } catch (SmackException.NotConnectedException e) {
                            L.error(e.getMessage());
                        }


                        db.close();



                    }

                }
            }).start();

        }
    };

    @Override
    public void onResume() {
        L.debug("MyBroadcastFragment, onResume");
        super.onResume();

        getActivity().registerReceiver(mActionUserProfileUpdatedReceived, new IntentFilter(AppConfig.ACTION_USER_PROFILE_UPDATED));

        RetrieveMyOwnBroadcast retrieveMyOwnBroadcast = new RetrieveMyOwnBroadcast();
        retrieveMyOwnBroadcast.onExecutePendingTask();

        //resetProfilePic();
        updateUI();
        /*/
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				SQLiteHandler db = new SQLiteHandler(getActivity().getApplicationContext());
				db.openToRead();
				
				final List<Announcement> announcements = db.downloadPersonalBroadcasts();
				
				mUsername = GroupChatHomeActivity.displayName(db.getName() + "", db.getUsername());
				
				db.close();
				
				
				mRvBroadcasts.post(new Runnable() {
					
					@Override
					public void run() {
						
						mAnouncements.clear();
						mAnouncements.addAll(announcements);
						
						L.debug("mAnouncements.size "+mAnouncements.size());
						adapter.notifyDataSetChanged();
					}
				});
				
			}
		}).start();
		//*/





    }


    private void updateUI() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                //*/
                SQLiteHandler db = new SQLiteHandler(getActivity());
                db.openToRead();

                final UserProfile profile = new UserProfile();
                profile.setUsername(db.getUsername());
                profile.downloadOffline(getActivity());

                 Bitmap rawImage = BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.pic_sample_girl);

                

                if (profile.getAvatarDir() != null && !profile.getAvatarDir().isEmpty()) {

                    // Get the dimensions of the View
                    int targetW = mImgProfile.getWidth();
                    int targetH = mImgProfile.getHeight();

                    BmpFactory bmpFactory = new BmpFactory();

                    Bitmap newImage = bmpFactory.getBmpWithTargetWTargetHFrm(targetW, targetH, profile.getAvatarDir());

                    if (newImage != null) rawImage = newImage;
                }
                final Bitmap avatar = rawImage;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        mImgProfile.setImageBitmap(avatar);

                        mTvJobTitle.setVisibility(View.GONE);
                        mTvUname.setVisibility(View.GONE);
                        mTvUrl0.setVisibility(View.GONE);
                        mTvUrl1.setVisibility(View.GONE);
                        mTvUrl2.setVisibility(View.GONE);


                        if (profile.getProfession() != null && !profile.getProfession().equals("")) {
                            mTvJobTitle.setVisibility(View.VISIBLE);
                            mTvJobTitle.setText(profile.getProfession());
                        }

                        if (profile.getUsername() != null && !profile.getUsername().equals("")) {
                            mTvUname.setVisibility(View.VISIBLE);
                            mTvUname.setText(profile.getUsername());
                        }

                        if (profile.getUrl0() != null && !profile.getUrl0().equals("")) {
                            mTvUrl0.setVisibility(View.VISIBLE);
                            mTvUrl0.setText(profile.getUrl0());
                        }

                        if (profile.getUrl1() != null && !profile.getUrl1().equals("")) {
                            mTvUrl1.setVisibility(View.VISIBLE);
                            mTvUrl1.setText(profile.getUrl1());
                        }

                        if (profile.getUrl2() != null && !profile.getUrl2().equals("")) {
                            mTvUrl2.setVisibility(View.VISIBLE);
                            mTvUrl2.setText(profile.getUrl2());
                        }

                    }
                });


                db.close();
                //*/

            }
        }).start();

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBroadId;
        TextView tvBroadFrm;
        TextView tvDateBroad;
        TextView tvLocLocal;
        TextView tvBroadMsg;
        TextView tvReach;
        TextView tvReply;
        ImageView ImgReply;
        LinearLayout btnReply;
        TextView tvBroadFrmRaw;

        public ViewHolder(View itemView) {
            super(itemView);

            tvBroadId = (TextView) itemView.findViewById(R.id.broad_id);
            tvBroadFrm = (TextView) itemView.findViewById(R.id.broad_from);
            tvDateBroad = (TextView) itemView.findViewById(R.id.date_broad);
            tvLocLocal = (TextView) itemView.findViewById(R.id.location_local);
            tvBroadMsg = (TextView) itemView.findViewById(R.id.broad_message);
            tvReach = (TextView) itemView.findViewById(R.id.reach);
            tvReply = (TextView) itemView.findViewById(R.id.txtReply);
            ImgReply = (ImageView) itemView.findViewById(R.id.imgReply);
            btnReply = (LinearLayout) itemView.findViewById(R.id.btnReply);
            tvBroadFrmRaw = (TextView) itemView.findViewById(R.id.broad_from_raw);


        }
    }


}
