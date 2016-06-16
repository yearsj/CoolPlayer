package yearsj.com.coolplayer.View.ui.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.model.MediaDescriptionInfo;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.AlbumArtCache;

/**
 * Created by yearsj on 2016/6/5.
 */
public class AlbumFragment extends BaseFragment {
    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    private ImageView albumCover;
    private View view = null;
    private TextView title;
    private TextView author;

    private MediaBrowserProvider mediaBrowserProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_cover, container, false);
        albumCover = (ImageView)view.findViewById(R.id.albumcover);
        title = (TextView)view.findViewById(R.id.title_play);
        author=(TextView)view.findViewById(R.id.author_paly);
        isPrepared = true;
        mHasLoadedOnce = false;
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

    @Override
    public void onStop() {
        super.onStop();
        MediaControllerCompat controller = ((FragmentActivity) getActivity()).getSupportMediaController();
        controller.unregisterCallback(mMediaControllerCallback);
    }

    protected void connect(){
        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mediaBrowserProvider.getMediaBrowser();
        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }


    protected void onConnected() {
        MediaControllerCompat controller = ((FragmentActivity) getActivity()).getSupportMediaController();
        if (controller != null) {
            refreshData(controller.getMetadata().getDescription());
            controller.registerCallback(mMediaControllerCallback);
        }
    }

    protected final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        //当前播放音乐改变
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                refreshData(metadata.getDescription());
            }
        }
    };


    //刷新界面
    private void refreshData(MediaDescriptionCompat descriptionCompat) {

        if(null!= descriptionCompat){
            title.setText(descriptionCompat.getTitle());
            author.setText("--"+descriptionCompat.getSubtitle()+"--");
            fetchImageAsync(descriptionCompat);
        }
    }

    //得到专辑封面
    private void fetchImageAsync(MediaDescriptionCompat description) {

        Bitmap bitmap = null;
        if (description.getIconUri() == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            albumCover.setImageBitmap(bitmap);
            return;
        }

        String artUrl = description.getIconUri().toString();
        final String currentArtUrl = artUrl;
        AlbumArtCache cache = AlbumArtCache.getInstance();
        bitmap = cache.getBigImage(artUrl);
        if (bitmap == null) {
            bitmap = description.getIconBitmap();
        }
        if (bitmap != null) {
            // if we have the art cached or from the MediaDescription, use it:
            albumCover.setImageBitmap(bitmap);
        } else {
            // otherwise, fetch a high res version and update:
            cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    // sanity check, in case a new fetch request has been done while
                    // the previous hasn't yet returned:
                    if (artUrl.equals(currentArtUrl)) {
                        albumCover.setImageBitmap(bitmap);
                    }
                }
            });
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
