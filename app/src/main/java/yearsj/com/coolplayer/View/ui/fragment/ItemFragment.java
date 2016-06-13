package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import yearsj.com.coolplayer.View.adapter.MusicItemAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.model.MusicItem;
import yearsj.com.coolplayer.View.ui.MainActivity;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class ItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_MEDIA_ID = "media_id";
    // TODO: Customize parameters
    TextView mainTitile;
    ListView listView;

    MusicItemAdapter musicItemAdapter;
    private ImageView backImage;
    View view;
    String mMediaId;
    private MediaBrowserProvider mediaBrowserProvider;
    private static final String TAG = LogHelper.makeLogTag(ItemFragment.class.getSimpleName());
    final String TITLE = "title";
    final String INFO = "info";
    List<MediaBrowserCompat.MediaItem> songs;

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            LogHelper.d(TAG, "Received state change: ", state);
            musicItemAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            LogHelper.d(TAG, "Received metadata change to media ",
                    metadata.getDescription().getMediaId());
            musicItemAdapter.notifyDataSetChanged();
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
                        musicItemAdapter.clear();
                        songs=children;
                        for (MediaBrowserCompat.MediaItem item : children) {
                            String title=item.getDescription().getTitle().toString();
                            String info=item.getDescription().getSubtitle().toString();
                            MusicItem musicItem=new MusicItem(title,info);
                            musicItemAdapter.add(musicItem);
                        }
                        musicItemAdapter.notifyDataSetChanged();
                        setListViewHeightBasedOnChildren(listView);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_item_list, container, false);
        initialView();
        return view;
    }


    void initialView(){
        mainTitile=(TextView)view.findViewById(R.id.mainTitile);
        musicItemAdapter=new MusicItemAdapter(getActivity(),R.layout.two_item_list);
        listView=(ListView) view.findViewById(R.id.musicListView);
        listView.setAdapter(musicItemAdapter);
        setOnListListener();

        mMediaId=getArguments().getString("mediaId");
        String title=mMediaId.substring(mMediaId.indexOf("/")+1);
        if(title.length()>7)
            title=title.substring(0,7)+"...";
        changeTitle(title);

        backImage= (ImageView)view.findViewById(R.id.back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).showMainContent();
            }
        });
    }

    public void changeTitle(CharSequence title){
        if (title == null) {
            title = getString(R.string.app_name);
        }
        mainTitile.setText(title);
    }

    public static final ItemFragment newInstance(String mediaId){
        ItemFragment itemFragment = new ItemFragment();
        Bundle bd = new Bundle();
        bd.putString("mediaId",mediaId);
        itemFragment.setArguments(bd);
        return itemFragment;
    }

    private void setOnListListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO 自动生成的方法存根
               MediaBrowserCompat.MediaItem playingMusic=songs.get(position);
                mediaBrowserProvider.onMediaItemSelected(playingMusic);

            }

        });
    }

    /**
     * 根据listView的数据个数动态的改变高度
     * @param listView
     */
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


    public void onConnected() {
        if (isDetached()) {
            return;
        }
        //  updateTitle();
        mediaBrowserProvider.getMediaBrowser().unsubscribe(mMediaId);

        mediaBrowserProvider.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = ((FragmentActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
    }


}
