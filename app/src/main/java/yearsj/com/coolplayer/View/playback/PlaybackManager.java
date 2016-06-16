package yearsj.com.coolplayer.View.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import yearsj.com.coolplayer.View.model.MusicProvider;
import yearsj.com.coolplayer.View.util.LogHelper;

public class PlaybackManager implements Playback.Callback {

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class.getSimpleName());
    public static final String ACTION_MODE = "action_mode";
    public static final String PLAY_MODE = "play_mode";      //播放模式
    public static final int SING_CYCLE = 1;     //单曲循环
    public static final int RANDOM_CYCLE = 2;   //随机播放
    public static final int LIST_LOOP = 0; //顺序播放

    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private Resources mResources;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;
    private int mCurrentPlayMode = LIST_LOOP;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                           QueueManager queueManager, MusicProvider musicProvider, Playback playback) {
        this.mServiceCallback = serviceCallback;
        this.mResources = resources;
        this.mQueueManager = queueManager;
        this.mMusicProvider = musicProvider;

        mMediaSessionCallback = new MediaSessionCallback();
        this.mPlayback = playback;
        mPlayback.setCallback(this);
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }
    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic,mCurrentPlayMode == SING_CYCLE);
        }
    }

    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        int state = mPlayback.getState();

        if (error != null) {
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }


    //随机播放
    private void onRomdomPlay(){
        if (mQueueManager.skipRomdomPosition()) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            // if skip is not possible, we stop and release the resources
            handleStopRequest(null);
        }
    }

    //单曲循环
    private void onSingePlay(){
            handlePlayRequest();
    }

    //列表循环
    private void onListLoop(){
        if (mQueueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            // if skip is not possible, we stop and release the resources
            handleStopRequest(null);
        }
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        switch (mCurrentPlayMode){
            case RANDOM_CYCLE:onRomdomPlay(); break;
            case SING_CYCLE: onSingePlay(); break;
            case LIST_LOOP: onListLoop(); break;
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        LogHelper.d(TAG, "setCurrentMediaId", mediaId);
        mQueueManager.setQueueFromMusic(mediaId);
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            LogHelper.d(TAG, "play");
            if (mQueueManager.getCurrentMusic() == null) {
                LogHelper.d(TAG, "No music got from current queue.");
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            LogHelper.d(TAG, "OnSkipToQueueItem:" + id);
            mQueueManager.setCurrentQueueItem(id);
            handlePlayRequest();
            mQueueManager.updateMetadata();
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "pause. current state=" + mPlayback.getState());
            handlePauseRequest();;
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSeekTo(long pos) {
            LogHelper.d(TAG, "onSeekTo:", pos);
            mPlayback.seekTo((int) pos);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            LogHelper.d(TAG, "playFromMediaId mediaId:", mediaId, "  extras=", extras);
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onSkipToNext() {
            LogHelper.d(TAG, "skipToNext");
            if ((mCurrentPlayMode==LIST_LOOP||mCurrentPlayMode==SING_CYCLE)&&mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else if(mCurrentPlayMode==RANDOM_CYCLE&&mQueueManager.skipRomdomPosition()){
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if ((mCurrentPlayMode==LIST_LOOP||mCurrentPlayMode==SING_CYCLE)&&mQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else if(mCurrentPlayMode==RANDOM_CYCLE&&mQueueManager.skipRomdomPosition()){
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if(action.equals(ACTION_MODE)){
                int mode = extras.getInt(PLAY_MODE);
                mCurrentPlayMode = mode;
            }
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
