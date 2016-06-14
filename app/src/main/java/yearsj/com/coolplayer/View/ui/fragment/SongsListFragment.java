package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yearsj.com.coolplayer.View.adapter.SortAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.model.SortModel;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.ui.view.CharacterSideBarView;
import yearsj.com.coolplayer.View.ui.view.SearchInfoTextView;
import yearsj.com.coolplayer.View.util.CharacterParser;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.PinyinComparator;

/**
 * Created by bing on 2016/6/2.
 */
public class SongsListFragment extends BaseFragment {

    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    /**
     * 事件列表
     **/
    private ListView list;
    private View view;
    LayoutInflater inflater;

    private SortAdapter adapter;
    private static CharacterSideBarView sideBar;
    private static TextView dialog;


    private String mMediaId;
    private PinyinComparator pinyinComparator;
    private CharacterParser characterParser;
    private List<SortModel> sourceDataList;
    private static int listViewHeight;

    private List<String>  titles;
    List<MediaBrowserCompat.MediaItem> songs;
    private MediaBrowserProvider mediaBrowserProvider;
    private SearchInfoTextView filterEdit;

    private static final String TAG = LogHelper.makeLogTag(SongsListFragment.class.getSimpleName());
    private static final String ARG_MEDIA_ID = "media_id";
    final String TITLE = "title";
    final String INFO = "info";

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

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                                "  count=" + children.size());
                        adapter.clear();
                        titles=new ArrayList<String>();
                        songs=children;
                        for (MediaBrowserCompat.MediaItem item : children) {
                            Map<String,String> map=new HashMap<String, String>();
                            String title=item.getDescription().getTitle().toString();
                            String info=item.getDescription().getSubtitle().toString();
                            map.put(TITLE, title);
                            map.put(INFO, info);

                            titles.add(title);
                            sourceDataList = filledData(titles);
                            Collections.sort(sourceDataList, pinyinComparator);
                            adapter.add(map,sourceDataList);
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

    public static final SongsListFragment newInstance(String mediaId){
        SongsListFragment songsListFragment = new SongsListFragment();
        Bundle bd = new Bundle();
        bd.putString("mediaId",mediaId);
        songsListFragment.setArguments(bd);
        return songsListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("yearsj", "fragment1-->onCreate()");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_songs, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
        initial();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("yearsj", "fragment1-->onCreateView()");

        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
            Log.v("yearsj", "fragment1-->移除已存在的View");
        }
        isPrepared = true;
        return view;
    }

    void initial() {
        filterEdit= (SearchInfoTextView) view.findViewById(R.id.filter_edit);
        list = (ListView) view.findViewById(R.id.songListView);
        sideBar = (CharacterSideBarView) view.findViewById(R.id.sidebars);
        dialog = (TextView)view.findViewById(R.id.adialog);
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        mMediaId=getArguments().getString("mediaId");

        loadAdapter();
        setOnListListener();

        setSideBarHight(listViewHeight);
        sideBar.setTextView(dialog, listViewHeight);
        sideBar.setOnTouchingLetterChangedListener(new CharacterSideBarView.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                final int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    list.setSelection(position);
                }

            }
        });
    }

    public static void setSideBarHight(int hight){
        listViewHeight=hight;
        if(sideBar!=null)
            sideBar.setTextView(dialog, hight);
    }

    /**
     * 设置适配器
     */
    void loadAdapter() {
        ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();

        sourceDataList = filledData(new ArrayList<String>());
        Collections.sort(sourceDataList, pinyinComparator);
        adapter = new SortAdapter(view.getContext(),
                mylist,
                R.layout.two_item_list,


                new String[]{ TITLE, INFO},


                new int[]{ R.id.titleView, R.id.infoView},sourceDataList);

     list.setAdapter(adapter);

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(firstVisibleItem==0){
                        filterEdit.setVisibility(View.VISIBLE);
                    }else{
                        filterEdit.setVisibility(view.GONE);
                    }
            }
        });
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
                MediaBrowserCompat.MediaItem playingMusic = songs.get(position);
                mediaBrowserProvider.onMediaItemSelected(playingMusic);
            }
        });

    }


    /**
     * 获取data的首字母
     * @param data
     * @return
     */
    private List<SortModel> filledData(List<String> data) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < data.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(data.get(i));

            String pinyin = characterParser.getSelling(data.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }
            mSortList.add(sortModel);
        }
        return mSortList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
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

    private void connect(){
        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mediaBrowserProvider.getMediaBrowser();

        LogHelper.v(TAG, "fragment.onStart, mediaId=" + mMediaId +
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    public void onConnected() {
        if (isDetached()) {
            return;
        }

      //  updateTitle();
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

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return true;
            }
        }.execute();
    }
}

