package yearsj.com.coolplayer.View.model;

import android.support.v4.media.MediaBrowserCompat;

public interface MediaBrowserProvider {
    MediaBrowserCompat getMediaBrowser();

    void onMediaItemSelected(MediaBrowserCompat.MediaItem playigMusic);
}
