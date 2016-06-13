package yearsj.com.coolplayer.View.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import yearsj.com.coolplayer.View.model.MusicItem;
import yearsj.com.coolplayer.View.ui.R;

/**
 * Created by bing on 2016/6/12.
 */
public class MusicItemAdapter extends ArrayAdapter<MusicItem>{

    Context context;
    int mResourceId;

    public MusicItemAdapter(Context context, int resource) {
        super(context, resource);
        this.context=context;
        this.mResourceId=resource;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        MusicItem musicItem = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(mResourceId, null);
        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        TextView infoView = (TextView) view.findViewById(R.id.infoView);

        titleView.setText(musicItem.getTitle());
        infoView.setText(musicItem.getInfo());

        return view;
    }

}
