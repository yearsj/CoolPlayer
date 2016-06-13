package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import yearsj.com.coolplayer.View.adapter.PlayListAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.ui.R;

/**
 * Created by yearsj on 2016/6/5.
 */
public class PlayListFragment extends Fragment {
    private PlayListAdapter playListAdapter = null;
    private ListView listView = null;
    private View view = null;
    private MediaBrowserProvider mediaBrowserProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playListAdapter = new PlayListAdapter(this.getActivity().getApplicationContext());
        listView.setAdapter(playListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaSessionCompat.QueueItem item = playListAdapter.getItem(position);
                MediaControllerCompat controller = ((FragmentActivity) getActivity()).getSupportMediaController();
                if (controller != null) {
                    controller.getTransportControls().skipToQueueItem(item.getQueueId());
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mediaBrowserProvider = (MediaBrowserProvider) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserCompat mediaBrowser = mediaBrowserProvider.getMediaBrowser();
        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = ((FragmentActivity) getActivity()).getSupportMediaController();
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
            List<MediaSessionCompat.QueueItem> queue = controller.getQueue();
            if (queue != null) {
                playListAdapter.clear();
                playListAdapter.notifyDataSetInvalidated();
                playListAdapter.addAll(queue);
                playListAdapter.notifyDataSetChanged();
            }
            onPlaybackStateChanged(controller.getPlaybackState());
        }
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            if (queue != null) {
                playListAdapter.clear();
                playListAdapter.notifyDataSetInvalidated();
                playListAdapter.addAll(queue);
                playListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            onPlaybackStateChanged(state);
        }
    };

    //根据状态更改图片状态
    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        playListAdapter.setActiveQueueItemId(state.getActiveQueueItemId());
        playListAdapter.notifyDataSetChanged();
    }
}
