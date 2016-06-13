package yearsj.com.coolplayer.View.model;

import android.support.v4.media.MediaBrowserCompat;

/**
 * Created by bing on 2016/6/12.
 */
public interface MediaFragmentListener extends MediaBrowserProvider {
    void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    void setMainTitle(CharSequence title);
    void setNavigationItem(String mediaId);
}
