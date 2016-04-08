
package com.lpoezy.nexpa.activities;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.Announcement;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.OnExecutePendingTaskListener;
import com.lpoezy.nexpa.objects.ProfilePicture;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.openfire.XMPPManager;
import com.lpoezy.nexpa.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.DateUtils;
import com.lpoezy.nexpa.utility.L;
import com.lpoezy.nexpa.utility.RoundedImageView;

import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class OthersBroadcastsFragment extends Fragment implements Correspondent.OnCorrespondentUpdateListener{
	
	List<Announcement> mAnouncements;
	//private MyBroascastsAdapter adapter;
	private RecyclerView mRvBroadcasts;
	protected String mUsername;
	
	
	public static OthersBroadcastsFragment newInstance(String name) {
		OthersBroadcastsFragment fragment = new OthersBroadcastsFragment();
		Bundle args = new Bundle();

		args.putString(OthersBroadcastActivity.TAG_USERNAME, name);
		fragment.setArguments(args);
		return fragment;
	}

	public OthersBroadcastsFragment() {
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
	private Correspondent mCorrespondent;
	protected long userId;
	

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_my_broadcasts, container, false);
		
		mRvBroadcasts = (RecyclerView)v.findViewById(R.id.rv_my_broadcasts);
		mUsername = getArguments().getString(OthersBroadcastActivity.TAG_USERNAME);
		
		//mRvChatHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
		mAnouncements = new ArrayList<Announcement>();
		
		//adapter = new MyBroascastsAdapter(getActivity());
		
		//userId = getArguments().getLong(OthersBroadcastActivity.TAG_USER_ID, -1);
		
		mAdapter = new ParallaxRecyclerAdapter<Announcement>(mAnouncements) {
			
			
			@Override
			public void onBindViewHolderImpl(android.support.v7.widget.RecyclerView.ViewHolder viewHolder,
					ParallaxRecyclerAdapter<Announcement> adapter, int position) {
				
				
				Announcement ann = adapter.getData().get(position);
				
				ViewHolder vh = (ViewHolder)viewHolder;
				
				vh .tvBroadMsg.setText(ann.getBody());
				vh.tvReply.setText("REACHED " + ann.getReach());
				vh.tvReply.setVisibility(View.INVISIBLE);
				vh.ImgReply.setBackgroundResource(R.drawable.btn_reach);
				vh.tvBroadFrm.setText(mUsername);
				
				DateUtils du = new DateUtils();
				String dateFormatted = du.getMinAgo(ann.getDate());
				
				vh.tvDateBroad.setText(dateFormatted);
				 
				vh.tvLocLocal.setVisibility(View.GONE);
				
				if(ann.getLocLocal()!=null && !ann.getLocLocal().isEmpty())
				{
					String strLoc = "near "+ ann.getLocLocal();
					
					vh.tvLocLocal.setText(strLoc);
					vh.tvLocLocal.setVisibility(TextView.VISIBLE);
				}
				
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
	                    0, LayoutParams.WRAP_CONTENT , 1.2f);
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
        View header = getActivity().getLayoutInflater().inflate(R.layout.activity_their_broadcasts_header, mRvBroadcasts, false);
        mAdapter.setParallaxHeader(header, mRvBroadcasts);
        mAdapter.setData(mAnouncements);
        mRvBroadcasts.setAdapter(mAdapter);
        
        mImgProfile = (ImageView) header.findViewById(R.id.img_profile);
        
        
        mTvJobTitle = (TextView)header.findViewById(R.id.tv_job_title);
        mTvUname = (TextView)header.findViewById(R.id.tv_uname);
        mTvUrl0 = (TextView)header.findViewById(R.id.tv_url0);
        mTvUrl1 = (TextView)header.findViewById(R.id.tv_url1);
        mTvUrl2 = (TextView)header.findViewById(R.id.tv_url2);

		mTvUname.setText(mUsername);

		mTvJobTitle.setVisibility(View.GONE);
		mTvUrl0.setVisibility(View.GONE);
		mTvUrl1.setVisibility(View.GONE);
		mTvUrl2.setVisibility(View.GONE);


		new Thread(new Runnable() {
			@Override
			public void run() {
				L.debug("loading vcard...");
				final UserProfile profile = new UserProfile();
				profile.setUsername(mUsername);
				profile.loadVCard(XMPPService.xmpp.connection);

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {

						if (profile.getAvatarImg() != null) {
							Bitmap rawImage = profile.getAvatarImg();
							mImgProfile.setImageBitmap(rawImage);
						}


						if (profile.getProfession() != null && !profile.getProfession().equalsIgnoreCase("null") && !profile.getProfession().equals("")) {
							mTvJobTitle.setVisibility(View.VISIBLE);
							mTvJobTitle.setText(profile.getProfession());
						}


						if (profile.getUrl0() != null && !profile.getUrl0().equalsIgnoreCase("null") && !profile.getUrl0().equals("")) {
							mTvUrl0.setVisibility(View.VISIBLE);
							mTvUrl0.setText(profile.getUrl0());
						}

						if (profile.getUrl1() != null && !profile.getUrl1().equalsIgnoreCase("null") && !profile.getUrl1().equals("")) {
							mTvUrl1.setVisibility(View.VISIBLE);
							mTvUrl1.setText(profile.getUrl1());
						}

						if (profile.getUrl2() != null && !profile.getUrl2().equalsIgnoreCase("null") && !profile.getUrl2().equals("")) {
							mTvUrl2.setVisibility(View.VISIBLE);
							mTvUrl2.setText(profile.getUrl2());
						}

					}
				});



			}
		}).start();

		return v;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		
	}

	private class RetrieveMyOwnBroadcast implements OnExecutePendingTaskListener {
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

						try {
							//L.debug("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
							node = mgr.getNode(mUsername + "-broadcast");

							node.subscribe(mUsername+"@198.154.106.139");
							List<Subscription> subs = node.getSubscriptions();
							String mySubid = null;

							for(int i = 0; i < subs.size(); i++)
							{
								L.debug(subs.get(i).getJid()+": "+subs.get(i).getId());
								if(subs.get(i).getJid().split("@")[0].equals(mUsername)){
									mySubid = subs.get(i).getId();
									break;
								}

							}
							//sun@198.154.106.139
							L.debug("mySubid: "+mySubid);
							Collection<PayloadItem<Item>> eventItems = node.getItems(100, mySubid);

							Gson gson = new Gson();
							List<Announcement>announcements = new  ArrayList<Announcement>();
							for(Item item : eventItems) {


								XmlPullParserFactory factory = null;
								try {
									factory = XmlPullParserFactory.newInstance();

									factory.setNamespaceAware(true);
									XmlPullParser xpp = factory.newPullParser();

									xpp.setInput( new StringReader( item.toXML()) );
									int eventType = xpp.getEventType();
									while (eventType != XmlPullParser.END_DOCUMENT) {
										if(eventType == XmlPullParser.START_DOCUMENT) {
											L.debug("Start document");
										} else if(eventType == XmlPullParser.START_TAG) {
											//L.debug("Start tag " + xpp.getName());
										} else if(eventType == XmlPullParser.END_TAG) {
											//L.debug("End tag " + xpp.getName());
										} else if(eventType == XmlPullParser.TEXT) {
											L.debug("Text " + xpp.getText());

											Announcement announcement = gson.fromJson(xpp.getText(), Announcement.class);
											announcements.add(announcement);

										}
										eventType = xpp.next();
									}
									L.debug("End document");


								} catch (XmlPullParserException e) {
									L.error(e.getMessage());
								} catch (IOException e) {
									L.error(e.getMessage());
								}

							}

							mAnouncements.clear();
							mAnouncements.addAll(announcements);
							Collections.reverse(mAnouncements);
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

		super.onResume();
		RetrieveMyOwnBroadcast retrieveMyOwnBroadcast = new RetrieveMyOwnBroadcast();
		retrieveMyOwnBroadcast.onExecutePendingTask();
	}
	

	static class ViewHolder extends RecyclerView.ViewHolder{

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
	
	@Override
	public void onCorrespondentUpdate() {
		Bitmap rawImage = mCorrespondent.getProfilePic();
		try{
			RoundedImageView riv = new RoundedImageView(getActivity());
	        final Bitmap circImage = riv.getCroppedBitmap(rawImage, 100);
	        
	        mImgProfile.post(new Runnable() {
				
				@Override
				public void run() {
					
					mImgProfile.setImageBitmap(circImage);
					
				}
			});
		}catch(Exception e){}
		
	}
	
	
}
