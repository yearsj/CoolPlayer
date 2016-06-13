package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import  android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import yearsj.com.coolplayer.View.adapter.SortAdapter;
import yearsj.com.coolplayer.View.model.MediaFragmentListener;
import yearsj.com.coolplayer.View.model.SortModel;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.ui.view.CharacterSideBarView;
import yearsj.com.coolplayer.View.util.CharacterParser;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;
import yearsj.com.coolplayer.View.util.PinyinComparator;


/**
 * Created by bing on 2016/6/2.
 */
public class SongsListFragment extends Fragment {
    /**
     * 事件列表
     **/
    private ListView list;
    private View view;
    LayoutInflater inflater;

    private SortAdapter adapter;
    private CharacterSideBarView sideBar;
    private TextView dialog;


    private String mMediaId;
    private PinyinComparator pinyinComparator;
    private CharacterParser characterParser;
    private List<SortModel> sourceDataList;
    private int listViewHeight,oneListHight;

    private List<String>  titles=new ArrayList<String>();
    List<MediaBrowserCompat.MediaItem> songs=new  ArrayList<MediaBrowserCompat.MediaItem>();
    private MediaFragmentListener mMediaFragmentListener;

    private static final String TAG = LogHelper.makeLogTag(SongsListFragment.class.getSimpleName());
    private static final String ARG_MEDIA_ID = "media_id";
    final String TITLE = "title";
    final String INFO = "info";

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
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



    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listViewHeight=getActivity().getWindowManager().getDefaultDisplay().getHeight()/3*2;
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

        return view;
    }

    void initial() {
        list = (ListView) view.findViewById(R.id.songListView);
        sideBar = (CharacterSideBarView) view.findViewById(R.id.sidebars);
        dialog = (TextView)view.findViewById(R.id.adialog);
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        loadData();
        setListViewHeightBasedOnChildren(list);
        setOnListListener();

        sideBar.setTextView(dialog, listViewHeight);
        sideBar.setOnTouchingLetterChangedListener(new CharacterSideBarView.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                final int position = adapter.getPositionForSection(s.charAt(0));
                System.out.println("position================"+position);
                Log.i("position",position+"");
                if (position != -1) {
                    list.requestFocus();
                    list.setItemChecked(position, true);
                    list.setSelectionFromTop(position,oneListHight);
                    list.smoothScrollToPosition(position);

                }

            }
        });
    }

    void loadData() {
        ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map;

        for (int i = 0; i < songs.size(); i++) {
            map = new HashMap<String, Object>();
            String title=songs.get(i).getDescription().getTitle().toString();
            String info=songs.get(i).getDescription().getSubtitle().toString();
            map.put(TITLE, title);
            map.put(INFO, info);
            titles.add(title);
            mylist.add(map);
        }

        sourceDataList = filledData(titles);
        Collections.sort(sourceDataList, pinyinComparator);
        adapter = new SortAdapter(view.getContext(),
                mylist,
                R.layout.two_item_list,


                new String[]{ TITLE, INFO},


                new int[]{ R.id.titleView, R.id.infoView},sourceDataList);

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                }
                return false;
            }
        });

     //   list.setAdapter(singerAdapter);
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


            }

        });

    }


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
        mMediaFragmentListener = (MediaFragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();

        LogHelper.v(TAG, "fragment.onStart, mediaId=" + mMediaId +
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerCompat controller = ((FragmentActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if(args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(SongsListFragment.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }


    public void onConnected() {
        if (isDetached()) {
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMediaFragmentListener.getMediaBrowser().getRoot();
        }
      //  updateTitle();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaId);

        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = ((FragmentActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
    }



}

