package com.lpoezy.nexpa.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.Correspondents;
import com.lpoezy.nexpa.objects.ListOfCollectionsIQ;
import com.lpoezy.nexpa.objects.NewMessage;
import com.lpoezy.nexpa.utility.DividerItemDecoration;
import com.lpoezy.nexpa.utility.L;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

public class ChatHistoryListFragment extends Fragment implements Correspondent.OnCorrespondentUpdateListener {

	private OnShowChatHistoryListener mCallback;
	// private List<Correspondent> mBuddys;
	private Correspondents mBuddys;
	public  ChatHistoryAdapter adapter;
	public List<ListOfCollectionsIQ.Chat> collections = new ArrayList<ListOfCollectionsIQ.Chat>();

	// private SwipeRefreshLayout mSwipeRefreshLayout;
	private SwipyRefreshLayout mSwipeRefreshLayout;
	private RecyclerView rvChatHistory;




	public static ChatHistoryListFragment newInstance() {
		ChatHistoryListFragment fragment = new ChatHistoryListFragment();
		return fragment;
	}

	public ChatHistoryListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		try {
			mCallback = (OnShowChatHistoryListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					activity.getClass().getSimpleName() + " must implement OnShowChatHistoryListener interface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_chat_history_list, container, false);

		rvChatHistory = (RecyclerView) v.findViewById(R.id.rv_chat_history);
		rvChatHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
		rvChatHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

		// mBuddys = new ArrayList<Correspondent>();
		mBuddys = new Correspondents();

		adapter = new ChatHistoryAdapter(getActivity(), mCallback);
		rvChatHistory.setAdapter(adapter);

		return v;
	}

	// receiving messages will be handle by receivedMessage
	// in ChatMessagesService
	private BroadcastReceiver mReceivedMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			L.debug("=============ChatHistoryList, message received================");

		}

	};

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		getActivity().unregisterReceiver(mReceivedMessage);
		// getActivity().unregisterReceiver(mReceivedCorrespondentUpdate);

		// for (int i = 0; i < mBuddys.size(); i++) {
		// Correspondent correspondent = mBuddys.get(i);
		// correspondent.removeListener(this);
		// }
	}

	@Override
	public void onResume() {
		L.debug("ChatHistory, onResume");
		super.onResume();

		getActivity().registerReceiver(mReceivedMessage, new IntentFilter(AppConfig.ACTION_RECEIVED_MSG));

		// getActivity().registerReceiver(mReceivedCorrespondentUpdate, new
		// IntentFilter(Correspondent.ACTION_UPDATE));
		// int count = OneComment.getUnReadMsgCountOffline(getActivity());
		// L.debug("count: "+count+", mBuddys.isEmpty: "+mBuddys.isEmpty());

	}

	// protected void downloadProfilePics(final List<Correspondent>
	// correspondents) {
	protected void downloadProfilePics(final Correspondents correspondents) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				final int MAX_THREAD = 5;
				int n = correspondents.size() < MAX_THREAD && correspondents.size() != 0 ? correspondents.size()
						: MAX_THREAD;
				ExecutorService exec = Executors.newFixedThreadPool(n);
				// L.debug("correspondents.size() "+correspondents.size());
				for (int i = 0; i < correspondents.size(); i++) {

					final Correspondent correspondent = correspondents.get(i);
					// L.debug("xxxxxxxxxx correspondent
					// username"+correspondent.getUsername()+"xxxxxxx");
					correspondent.addListener(ChatHistoryListFragment.this);

					exec.execute(new Runnable() {

						@Override
						public void run() {
							correspondent.downloadCorrespondentIdOffline(getActivity());
							long userId = correspondent.getId();
							correspondent.downloadProfilePicOnline(getActivity(), userId);

						}
					});

				}
				exec.shutdown();
				try {
					exec.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) {

				}

			}
		}).start();

	}

	public void setCollections(List<ListOfCollectionsIQ.Chat> collections) {
		this.collections.clear();

		this.collections.addAll(collections);
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {

				adapter.notifyDataSetChanged();
			}
		});

	}

	private class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

		private LayoutInflater inflater;
		// private List<Correspondent> buddys;
		private OnShowChatHistoryListener listener;

		public ChatHistoryAdapter(Context context, OnShowChatHistoryListener listener) {
			this.inflater = LayoutInflater.from(context);
			// this.buddys = buddys;
			this.listener = listener;
		}

		@Override
		public int getItemCount() {

			return collections.size();
		}

		@Override
		public void onBindViewHolder(ViewHolder vh, int position) {
			vh.position = position;
			String name = collections.get(position).getWith().split("@")[0];

			vh.tvBuddys.setText(name);
			vh.tvMsg.setText(collections.get(position).getStart());
			vh.tvMsgDate.setVisibility(View.INVISIBLE);

		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
			View itemView = inflater.inflate(R.layout.row_chat_history, parent, false);
			return new ViewHolder(itemView);
		}

		class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

			TextView tvBuddys;
			TextView tvMsg;
			TextView tvMsgDate;
			ImageView imgProfilePic;
			int position;

			public ViewHolder(View view) {
				super(view);
				tvBuddys = (TextView) view.findViewById(R.id.tv_buddys_name);
				tvMsg = (TextView) view.findViewById(R.id.tv_buddys_msg);
				tvMsgDate = (TextView) view.findViewById(R.id.tv_buddys_msg_date);
				imgProfilePic = (ImageView) view.findViewById(R.id.img_profile_pic);

				view.setOnClickListener(this);
			}

			@Override
			public void onClick(View v) {
				//L.debug("with: "+tvBuddys.getText().toString()+", start: "+tvMsg.getText().toString());
				mCallback.onShowChatHistory(tvBuddys.getText().toString(), tvMsg.getText().toString());
			}

		}
	}

	public interface OnShowChatHistoryListener {
		public void onShowChatHistory(String with, String start);
		//public void onShowChatHistory(Correspondent buddy);
	}

	@Override
	public void onCorrespondentUpdate() {

		rvChatHistory.post(new Runnable() {

			@Override
			public void run() {
				L.debug("ChatHistoryList, onCorrespondentUpdate");
				adapter.notifyDataSetChanged();
			}
		});
	}

}
