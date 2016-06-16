package yearsj.com.coolplayer.View.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
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
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import yearsj.com.coolplayer.View.adapter.MyFragmentAdapter;
import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.model.MediaDescriptionInfo;
import yearsj.com.coolplayer.View.playback.PlaybackManager;
import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.fragment.AlbumFragment;
import yearsj.com.coolplayer.View.ui.fragment.PlayListFragment;
import yearsj.com.coolplayer.View.util.AlbumArtCache;

/**
 * Created by yearsj on 2016/6/4.
 */
public class PlayActivity extends FragmentActivity implements View.OnClickListener,MediaBrowserProvider,MediaDescriptionInfo{

    private ViewPager mViewPager;
    private MyFragmentAdapter myFragmentAdapter;
    private List<Fragment> fragments;
    private View dot1;
    private View dot2;
    private ImageView list_play;

    private TextView endText;
    private TextView startText;
    private ImageView background;
    private ImageView playStatus;
    private ImageView next;
    private ImageView pre;
    private ImageView play_mode;
    private SeekBar processSeekBar;

    private Timer timer = new Timer();

    private Bitmap albumCover;
    private String currentArtUrl;
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackStateCompat mPlaybackState;
    private MediaDescriptionCompat mCurrentDescription;
    private int currentPage = 0;
    private int mode = 0;
    private PlayListFragment playListFragment;

    /**监听对话框里面的button点击事件*/
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            finish();
            overridePendingTransition(R.anim.just_stay, R.anim.slide_up_to_bottom);
        }
        return false;
    }


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
    }

    //更新控件
    private void updateCompnent(MediaDescriptionCompat description){
        if(null!=description){
            fetchImageAsync(description);
        }

        mCurrentDescription = description;
        if(fragments == null){
            fragments = new ArrayList<Fragment>();
            fragments.add(new AlbumFragment());
            playListFragment = new PlayListFragment();
            fragments.add(playListFragment);
            myFragmentAdapter = new MyFragmentAdapter(this.getSupportFragmentManager(), fragments);
            mViewPager.setAdapter(myFragmentAdapter);
        }

        changeDot(currentPage);
        mViewPager.setCurrentItem(currentPage);
    }

    private void initView(){
        mViewPager = (ViewPager)findViewById(R.id.viewpager_play);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        dot1 = (View)findViewById(R.id.dot_1);
        dot2 = (View)findViewById(R.id.dot_2);
        list_play = (ImageView)findViewById(R.id.list_play);
        list_play.setOnClickListener(this);
        endText = (TextView)findViewById(R.id.endText);
        startText = (TextView)findViewById(R.id.startText);
        background = (ImageView)findViewById(R.id.full_background);
        playStatus = (ImageView)findViewById(R.id.play_tatus);
        playStatus.setOnClickListener(this);
        play_mode = (ImageView)findViewById(R.id.play_mode);
        play_mode.setOnClickListener(this);
        next  = (ImageView)findViewById(R.id.next_play);
        next.setOnClickListener(this);
        pre  = (ImageView)findViewById(R.id.pre_play);
        pre.setOnClickListener(this);
        processSeekBar = (SeekBar)findViewById(R.id.process);
        processSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                startText.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSupportMediaController().getTransportControls().seekTo(seekBar.getProgress());
                scheduleTimer();
            }
        });
    }


    //得到专辑封面
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
                updateDuration(metadata);
            }
        }
    };

    //连接session
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                PlayActivity.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }

        mediaController.registerCallback(mControllerCallback);
        setSupportMediaController(mediaController);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateCompnent(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleTimer();
        }
    }

    //根据播放状态修改界面
    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mPlaybackState = state;

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                playStatus.setImageResource(R.drawable.pause);
                scheduleTimer();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_BUFFERING:
                playStatus.setImageResource(R.drawable.play);
                stopTimer();
                break;
        }
    }

    //更新进度条
    private void updateProgress(){
        if (mPlaybackState == null) {
            return;
        }
        //上一次状态改变的位置
        long currentPosition = mPlaybackState.getPosition();
        if (mPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
            long timeDelta = SystemClock.elapsedRealtime() - mPlaybackState.getLastPositionUpdateTime();
            //加上状态改变后的长度
            currentPosition += (int) timeDelta * mPlaybackState.getPlaybackSpeed();
        }
        processSeekBar.setProgress((int) currentPosition);
    }

    //根据音乐播放时长修改界面
    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        processSeekBar.setMax(duration);
        endText.setText(DateUtils.formatElapsedTime(duration / 1000));
    }


    Handler handler = new Handler();
    Runnable updateSeekBar = new Runnable(){
        public void run() {
            updateProgress();
        }
    };

    //打开计时器
    private void scheduleTimer() {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(updateSeekBar);
                }
            },100,1000);
    }

    //停止计时器
    private void stopTimer(){
        if (timer!=null) {
            timer.cancel();
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
                            //stopTimer();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            transportControls.play();
                            //scheduleTimer();
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
            case R.id.play_mode:
                changeMode(transportControls);
                break;
        }
    }

    //更改播放模式
    private void changeMode(MediaControllerCompat.TransportControls transportControls){
        Bundle bundle = new Bundle();
        switch (mode){
            case 0: mode = 1; bundle.putInt(PlaybackManager.PLAY_MODE,PlaybackManager.SING_CYCLE);
                transportControls.sendCustomAction(PlaybackManager.ACTION_MODE, bundle);
                play_mode.setImageResource(R.drawable.single_play);
                Toast.makeText(this,"单曲循环",Toast.LENGTH_SHORT).show();
                break;
            case 1: mode = 2; bundle.putInt(PlaybackManager.PLAY_MODE, PlaybackManager.RANDOM_CYCLE);
                transportControls.sendCustomAction(PlaybackManager.ACTION_MODE, bundle);
                play_mode.setImageResource(R.drawable.play_random_play);
                Toast.makeText(this,"随机播放",Toast.LENGTH_SHORT).show();
                break;
            case 2: mode = 0; bundle.putInt(PlaybackManager.PLAY_MODE,PlaybackManager.LIST_LOOP);
                transportControls.sendCustomAction(PlaybackManager.ACTION_MODE, bundle);
                play_mode.setImageResource(R.drawable.play_list_loop);
                Toast.makeText(this,"列表循环",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem playigMusic) {
        return;
    }

    @Override
    public MediaDescriptionCompat getCurrentMediaDescription() {
        return mCurrentDescription;
    }

    @Override
    public Bitmap getCurrentMediaBitmap() {
        return albumCover;
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
            currentPage = arg0;
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
