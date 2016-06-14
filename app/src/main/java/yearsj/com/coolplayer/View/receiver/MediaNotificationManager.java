package yearsj.com.coolplayer.View.receiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.PlayActivity;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.AlbumArtCache;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.ResourceHelper;

public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = LogHelper.makeLogTag(MediaNotificationManager.class.getSimpleName());

    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "yearsj.com.coolplayer.pause";
    public static final String ACTION_PLAY = "yearsj.com.coolplayer.play";
    public static final String ACTION_PREV = "yearsj.com.coolplayer.prev";
    public static final String ACTION_NEXT = "yearsj.com.coolplayer.next";

    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;

    private final int mNotificationColor;

    private boolean mStarted = false;

    public MediaNotificationManager(MusicService service) throws RemoteException {
        this.mService = service;
        updateSessionToken();

        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary, Color.DKGRAY);

        mNotificationManager = NotificationManagerCompat.from(mService);

        String pkg = mService.getPackageName();

        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_UPDATE_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_UPDATE_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_UPDATE_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager.cancelAll();

    }

    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mMediaControllerCallback);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mMediaControllerCallback);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }


    private Notification createNotification() {
        LogHelper.d(TAG, "updateNotificationMetadata. mMetadata=" + mMetadata);
        if (mMetadata == null || mPlaybackState == null) {
            return  null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        int playPauseButtonPostion = 0;

        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(R.drawable.pre_play,mService.getString(R.string.label_previous), mPreviousIntent );
            playPauseButtonPostion = 1;
        }

        addPlayPauseAction(notificationBuilder);

        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(R.drawable.next,
                    mService.getString(R.string.label_next), mNextIntent);
        }

        MediaDescriptionCompat description = mMetadata.getDescription();

        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {
            // This sample assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            String artUrl = description.getIconUri().toString();
            art = AlbumArtCache.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                // use a placeholder art while the remote art is being downloaded
                art = BitmapFactory.decodeResource(mService.getResources(),
                        R.mipmap.ic_launcher);
            }
        }


        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(
                        new int[]{playPauseButtonPostion}
                ).setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(art)
                .setSmallIcon(R.drawable.background);



        setNotificationPlaybackState(notificationBuilder);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
        }
        return notificationBuilder.build();
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent intent = new Intent(mService, PlayActivity.class) ;
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (description != null) {
            //intent.putExtra(PlayActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }
        return PendingIntent.getActivity(mService, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void addPlayPauseAction(NotificationCompat.Builder notificationBuilder) {
        LogHelper.d(TAG, "updatePlayPauseAction");
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mService.getString(R.string.label_pause);
            icon = R.mipmap.play;
            intent = mPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.mipmap.pause;
            intent = mPlayIntent;
        }
        notificationBuilder.addAction(new NotificationCompat.Action(icon, label, intent));
    }
    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null || !mStarted) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            mService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. updating playback position to ",
                    (System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000, " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            LogHelper.d(TAG, "updateNotificationPlaybackState. hiding playback position");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,final NotificationCompat.Builder builder) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (mMetadata != null && mMetadata.getDescription().getIconUri() != null &&
                        mMetadata.getDescription().getIconUri().toString().equals(artUrl)) {
                    // If the media is still the same, update the notification:
                    LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", artUrl);
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }


    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mMediaControllerCallback);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mMediaControllerCallback);
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            default:
                LogHelper.w(TAG, "Unknown intent, ignored. Action = " + action);
        }

    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            LogHelper.d(TAG, "Received new metadata ", metadata);
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mPlaybackState = state;
            LogHelper.d(TAG, "Received new playback state", state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            LogHelper.d(TAG, "Session was destroyed, resetting to the new session token");
            try {
                updateSessionToken();
            } catch (RemoteException ex) {
                LogHelper.e(TAG, ex,  "could not connect to media controller");
            }
        }
    };
}
