package yearsj.com.coolplayer.View.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.session.MediaSession;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import yearsj.com.coolplayer.R;

/**
 * Created by yearsj on 2016/6/5.
 */
public class PlayListAdapter extends ArrayAdapter<MediaSession.QueueItem> {

    private long mActiveQueueItemId = MediaSession.QueueItem.UNKNOWN_ID;
    //自定义类
    private static class ViewHolder {
        ImageView play_status;
        TextView title;
        TextView author;
        TextView album;
    }

    private Context context;

    public PlayListAdapter(Context context) {
        super(context, R.layout.playlist, new ArrayList<MediaSession.QueueItem>());
        this.context = context;
    }

    public void setActiveQueueItemId(long id) {
        this.mActiveQueueItemId = id;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.playlist, parent, false);
            holder = new ViewHolder();
            holder.play_status = (ImageView) convertView.findViewById(R.id.play_status_list);
            holder.title = (TextView) convertView.findViewById(R.id.title_play_list);
            holder.author = (TextView) convertView.findViewById(R.id.author_play_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MediaSession.QueueItem item = getItem(position);
        holder.title.setText(item.getDescription().getTitle());
        holder.author.setText(item.getDescription().getDescription());

        // If the itemId matches the active Id then use a different icon
        if (mActiveQueueItemId == item.getQueueId()){
            holder.play_status.setImageDrawable(context.getDrawable(R.drawable.playing));
        } else{
            holder.play_status.setImageDrawable(context.getDrawable(R.drawable.play2));
        }
        return convertView;
    }
}
