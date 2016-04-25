package com.lpoezy.nexpa.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.DeleteMessageArchiveIQ;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ChatHistoryOptionsFragment#newInstance} factory method to create an instance
 * of this fragment.
 *
 */
public class ChatHistoryOptionsFragment extends DialogFragment {

	public static final String TAG = "ChatHistoryOptionsFragment";
	protected static final String UNAME = "UNAME";


	public static ChatHistoryOptionsFragment newInstance(String with) {
		ChatHistoryOptionsFragment fragment = new ChatHistoryOptionsFragment();
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		Bundle args = new Bundle();
		args.putString(UNAME, with);
		fragment.setArguments(args);

		return fragment;
	}

	protected String mCurrentPhotoPath;

	public ChatHistoryOptionsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		//Utilz.saveToSharedPref(getActivity(), ProfilePicture.DIR, "");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_chat_history_options, container, false);
		final String uname = getArguments().getString(UNAME);
		((LinearLayout) v.findViewById(R.id.btn_delete_chat_history)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
//						SQLiteHandler db = new SQLiteHandler(getActivity());
//						db.openToWrite();
//						int delRowCount = db.deleteChatHistoryWith(uname);
//						L.debug("uname: "+uname+", delRowCount: "+delRowCount);
//						db.close();
						//http://xmpp.org/extensions/xep-0136.html
						L.error("=============================================");
						DeleteMessageArchiveIQ iq = new DeleteMessageArchiveIQ();
						iq.setType(IQ.Type.set);
						try {
							XMPPService.xmpp.connection.sendStanza(iq);
						} catch (SmackException.NotConnectedException e) {
							L.error(""+e.getMessage());
						}


					}
				}).start();

			}
		});

		return v;
	}

}
