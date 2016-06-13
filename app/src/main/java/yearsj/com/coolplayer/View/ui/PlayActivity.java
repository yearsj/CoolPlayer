package yearsj.com.coolplayer.View.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import yearsj.com.coolplayer.View.adapter.MyFragmentAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.fragment.AlbumFragment;
import yearsj.com.coolplayer.View.ui.fragment.PlayListFragment;
import yearsj.com.coolplayer.View.util.AlbumArtCache;
import yearsj.com.coolplayer.View.util.LogHelper;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by yearsj on 2016/6/4.
 */
public class PlayActivity extends FragmentActivity implements View.OnClickListener,MediaBrowserProvider {

    private ViewPager mViewPager;
    private List<Fragment> fragments;
    private View dot1;
    private View dot2;
    private ImageView list_play;
    private TextView title;
    private TextView author;
    private ImageView background;
    private ImageView playStatus;
    private ImageView next;
    private ImageView pre;
    private SeekBar processSeekBar;

    private Bitmap albumCover;
    private String currentArtUrl;
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackStateCompat mPlaybackState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initView();

        //获得从前面一控件返回的播放音乐信息
        Intent intent = getIntent();
        if (intent != null) {
            MediaBrowserCompat.MediaItem mediaItem = intent.getParcelableExtra("mediaItem");
            if (null!=mediaItem) {
                updateCompnent(mediaItem.getDescription());
            }
        }

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getSupportMediaController() != null) {
            getSupportMediaController().unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    private void updateCompnent(MediaDescriptionCompat description){
        if(null!=description){
            title.setText(description.getTitle());
            author.setText(description.getSubtitle());
            fetchImageAsync(description);
        }

        fragments = new ArrayList<Fragment>();
        fragments.add(AlbumFragment.newInstance(albumCover));
        fragments.add(new PlayListFragment());
        mViewPager.setAdapter(new MyFragmentAdapter(this.getSupportFragmentManager(), fragments));
        changeDot(0);
    }

    private void initView(){
        mViewPager = (ViewPager)findViewById(R.id.viewpager_play);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        dot1 = (View)findViewById(R.id.dot_1);
        dot2 = (View)findViewById(R.id.dot_2);
        list_play = (ImageView)findViewById(R.id.list_play);
        list_play.setOnClickListener(this);
        title = (TextView)findViewById(R.id.title_play);
        author=(TextView)findViewById(R.id.author_paly);
        background = (ImageView)findViewById(R.id.full_background);
        playStatus = (ImageView)findViewById(R.id.play_tatus);
        playStatus.setOnClickListener(this);
        next  = (ImageView)findViewById(R.id.next_play);
        next.setOnClickListener(this);
        pre  = (ImageView)findViewById(R.id.pre_play);
        pre.setOnClickListener(this);
        processSeekBar = (SeekBar)findViewById(R.id.process);
    }

    private void fetchImageAsync(MediaDescriptionCompat description) {
        final Bitmap[] tempBitmap = new Bitmap[1];
        if (description.getIconUri() == null) {
            albumCover = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            background.setImageBitmap(albumCover);
            return;
        }
        String artUrl = description.getIconUri().toString();
        currentArtUrl = artUrl;
        AlbumArtCache cache = AlbumArtCache.getInstance();
        albumCover = cache.getBigImage(artUrl);
        if (albumCover == null) {
            albumCover = description.getIconBitmap();
        }
        if (albumCover != null) {
            // if we have the art cached or from the MediaDescription, use it:
            background.setImageBitmap(albumCover);
        } else {
            // otherwise, fetch a high res version and update:
            cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    // sanity check, in case a new fetch request has been done while
                    // the previous hasn't yet returned:
                    if (artUrl.equals(currentArtUrl)) {
                        background.setImageBitmap(bitmap);
                        tempBitmap[0] = bitmap;
                    }
                }
            });
            albumCover = tempBitmap[0];
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };

    private final MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        //当前播放状态的改变
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            updatePlaybackState(state);
        }

        @Override
        //当前播放音乐改变
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateCompnent(metadata.getDescription());
                //updateDuration(metadata);
            }
        }
    };

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                PlayActivity.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }
        setSupportMediaController(mediaController);
        mediaController.registerCallback(mControllerCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateCompnent(metadata.getDescription());
            //updateDuration(metadata);
        }
        //updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            //scheduleSeekbarUpdate();
        }
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mPlaybackState = state;

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                playStatus.setImageResource(R.drawable.pause);
                //scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_BUFFERING:
                playStatus.setImageResource(R.drawable.play);
                //stopSeekbarUpdate();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.list_play:
                mViewPager.setCurrentItem(1);
                break;
            default:playControl(v.getId());
                break;

        }
    }


    private void playControl(int id){
        MediaControllerCompat controllerCompat = getSupportMediaController();
        MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
        switch (id){
            case R.id.play_tatus:
                PlaybackStateCompat state = controllerCompat.getPlaybackState();
                if (state != null) {
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            transportControls.pause();
                            //stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            transportControls.play();
                            //scheduleSeekbarUpdate();
                            break;
                    }
                }
                break;
            case R.id.next_play:
                transportControls.skipToNext();
                break;
            case R.id.pre_play:
                transportControls.skipToPrevious();
                break;
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem playigMusic) {

    }

    /**
     * 内部类----viewpager监听器
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onPageSelected(int arg0) {
            // TODO Auto-generated method stub
            changeDot(arg0);
        }
    }

    private void changeDot(int index) {
        switch (index) {
            case 0:
                ViewGroup.LayoutParams lp1 = dot1.getLayoutParams();
                lp1.width = 8;
                lp1.height = 8;
                dot1.setLayoutParams(lp1);
                ViewGroup.LayoutParams np1 = dot2.getLayoutParams();
                np1.width = 6;
                np1.height = 6;
                dot2.setLayoutParams(np1);
                break;
            case 1:
                ViewGroup.LayoutParams lp2 = dot2.getLayoutParams();
                lp2.width = 8;
                lp2.height = 8;
                dot2.setLayoutParams(lp2);
                ViewGroup.LayoutParams np2 = dot1.getLayoutParams();
                np2.width = 6;
                np2.height = 6;
                dot1.setLayoutParams(np2);
                break;
            default:
                break;
        }
    }

    public void hidePlayInfo(View v){
        finish();
        overridePendingTransition( R.animator.popup_window_enter,R.anim.slide_up_to_bottom);
    }
}
