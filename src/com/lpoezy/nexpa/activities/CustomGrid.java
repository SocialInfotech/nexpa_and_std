package com.lpoezy.nexpa.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.UserProfile;
import com.lpoezy.nexpa.utility.L;

import java.util.ArrayList;

public class CustomGrid extends BaseAdapter {
    private Context mContext;
    // private final String[] web;
    private ArrayList<String> web = new ArrayList<String>();
    private ArrayList<String> availabilty = new ArrayList<String>();
    private ArrayList<Integer> Imageid = new ArrayList<Integer>();
    private ArrayList<Integer> distance = new ArrayList<Integer>();
    private ArrayList<Correspondent> mCorrespondents;
    private ArrayList<Long> userIds;


    public CustomGrid(Context c, ArrayList<String> web, ArrayList<Correspondent> correspondents/*ArrayList<Integer> Imageid*/, ArrayList<String> availabilty, ArrayList<Integer> distance) {
        mContext = c;
        //this.Imageid = Imageid;
        this.userIds = userIds;
        this.distance = distance;
        this.web = web;
        this.availabilty = availabilty;
        this.mCorrespondents = correspondents;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return web.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void removeItem(int position) {

        web.remove(position);
        Imageid.remove(position);
        availabilty.remove(position);
        distance.remove(position);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {


        View grid;
        grid = new View(mContext);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate(R.layout.grid_single, null);


        } else {
            grid = (View) convertView;
        }

        TextView textView = (TextView) grid.findViewById(R.id.grid_text);
        final ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
        ImageView offline = (ImageView) grid.findViewById(R.id.offline);
        //TextView txtAvailView = (TextView) grid.findViewById(R.id.txtAvail);


        Bitmap rawImage = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.pic_sample_girl);
        int avalability = R.drawable.offline;

        try {


            textView.setText(web.get(position)/*+  "|" */ /*+ distance.get(position) +"m" +avalability*/);
            //imageView.setImageResource(Imageid.get(position));


            avalability = mCorrespondents.get(position).isAvailable() ? R.drawable.online : R.drawable.offline;


            if (mCorrespondents.get(position).getProfilePic() != null) {
                rawImage = mCorrespondents.get(position).getProfilePic();
            }

        } catch (IndexOutOfBoundsException e) {
            L.error(e.getMessage());
        }

        offline.setImageResource(avalability);
        imageView.setImageBitmap(rawImage);

        return grid;
    }

}