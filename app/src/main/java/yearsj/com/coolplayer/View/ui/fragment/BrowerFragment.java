package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import yearsj.com.coolplayer.View.adapter.MusicItemAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.model.MusicItem;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.LogHelper;

public class BrowerFragment extends BaseFragment {
    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    private Drawable poster;
    /**
     * 事件列表
     **/
    private ListView list;
    private View view;
    LayoutInflater inflater;
    private static final String ARG_MEDIA_ID = "media_id";
    private String mMediaId;
    private MediaBrowserProvider mediaBrowserProvider;
    private static final String TAG = LogHelper.makeLogTag(BrowerFragment.class.getSimpleName());

    List<MediaBrowserCompat.MediaItem> browers;

    final String POSTER = "poster";
    final String TITLE = "title";
    final String INFO = "info";
    MusicItemAdapter adapter;

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            LogHelper.d(TAG, "Received state change: ", state);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            LogHelper.d(TAG, "Received metadata change to media ",
                    metadata.getDescription().getMediaId());
            adapter.notifyDataSetChanged();
        }
    };


    public static final BrowerFragment newInstance(String mediaId){
        BrowerFragment browerFragment = new BrowerFragment();
        Bundle bd = new Bundle();
        bd.putString("mediaId",mediaId);
        browerFragment.setArguments(bd);
        return browerFragment;
    }


    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                                "  count=" + children.size());
                        adapter.clear();
                        browers=children;
                        for (MediaBrowserCompat.MediaItem item : children) {
                            String title=item.getDescription().getTitle().toString();
                            String info=item.getDescription().getSubtitle().toString();

                            MusicItem musicItem=new MusicItem(title,info,R.mipmap.ic_launcher);
                            adapter.add(musicItem);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        LogHelper.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
                    Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                }
            };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("yearsj", "album-->onCreate()");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_brower, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
        isPrepared = true;
        initial();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("yearsj", "album-->onCreateView()");

        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
            Log.v("yearsj", "fragment1-->移除已存在的View");
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mediaBrowserProvider = (MediaBrowserProvider) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mediaBrowserProvider = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        lazyLoad();
    }

    private void connect(){
        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mediaBrowserProvider.getMediaBrowser();

        LogHelper.v(TAG, "fragment.onStart, mediaId=" + mMediaId +
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mediaBrowserProvider.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerCompat controller = ((FragmentActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
    }


    void initial() {
        list = (ListView) view.findViewById(R.id.albumListView);
        loadData();
        setOnListListener();
        mMediaId=getArguments().getString("mediaId");
    }

    void loadData() {
        adapter=new MusicItemAdapter(view.getContext(),R.layout.two_item_with_img_list);
        list.setAdapter(adapter);
    }


    /**
     * 列表子项点击事件响应
     */
    private void setOnListListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO 自动生成的方法存根
                MediaBrowserCompat.MediaItem browerItem = browers.get(position);
                mediaBrowserProvider.onMediaItemSelected(browerItem);
            }

        });

    }



    public void onConnected() {
        if (isDetached()) {
            return;
        }
        mediaBrowserProvider.getMediaBrowser().unsubscribe(mMediaId);

        mediaBrowserProvider.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        MediaControllerCompat controller = ((FragmentActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
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
