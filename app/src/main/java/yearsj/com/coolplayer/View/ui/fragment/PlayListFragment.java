package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import yearsj.com.coolplayer.View.adapter.PlayListAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.ui.R;

/**
 * Created by yearsj on 2016/6/5.
 */
public class PlayListFragment extends BaseFragment {
    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    private PlayListAdapter playListAdapter = null;
    private ListView listView = null;
    private View view = null;
    private MediaBrowserProvider mediaBrowserProvider;
    List<MediaSessionCompat.QueueItem> currentQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playListAdapter = new PlayListAdapter(this.getActivity().getApplicationContext());
        listView = (ListView)view.findViewById(R.id.play_list);
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

        isPrepared = true;
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
        lazyLoad();
    }
    private void connect(){
        // fetch browsing information to fill the listview:
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
                currentQueue = queue;
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
                currentQueue = queue;
                playListAdapter.addAll(queue);
                playListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            PlayListFragment.this.onPlaybackStateChanged(state);
        }
    };

    //根据状态更改图片状态
    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        playListAdapter.clear();
        //playListAdapter.notifyDataSetInvalidated();
        playListAdapter.addAll(currentQueue);
        playListAdapter.setActiveQueueItemId(state.getActiveQueueItemId());
        playListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //显示加载进度对话框
                System.out.println("正在加载...");
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if (isSuccess) {
                    // 加载成功
                    connect();
                    mHasLoadedOnce = true;
                } else {
                    // 加载失败
                    Log.i("DailyFragment", "加载失败");
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return true;
            }
        }.execute();
    }
}
