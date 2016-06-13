package yearsj.com.coolplayer.View.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class.getSimpleName());
    private MusicProviderSource mSource;

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByAlbum;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListBySinger;
    private List<MediaMetadataCompat> mAllMusicList;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider(Context context) {
        this(new LocalMediaSource(context));
    }
    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByAlbum = new ConcurrentHashMap<>();
        mMusicListBySinger = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mAllMusicList = new ArrayList<>();

    }

    /**
     * Get an iterator over the list of albums
     *
     * @return genres
     */
    public Iterable<String> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.keySet();
    }

    /**
     * Get an iterator over the list of albums
     *
     * @return genres
     */
    public Iterable<String> getSingers() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListBySinger.keySet();
    }


    /**
     * Get music tracks of the given album
     *
     */
    public Iterable<MediaMetadataCompat> getMusicsByAlbum(String album) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByAlbum.containsKey(album)) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.get(album);
    }

    /**
     * Get music tracks of the given album
     *
     */
    public Iterable<MediaMetadataCompat> getMusicsBySinger(String singer) {
        if (mCurrentState != State.INITIALIZED || !mMusicListBySinger.containsKey(singer)) {
            return Collections.emptyList();
        }
        return mMusicListBySinger.get(singer);
    }

    /**
     * Get all music tracks for root view
     *
     */
    public Iterable<MediaMetadataCompat> getAllMusics() {
        if (mCurrentState != State.INITIALIZED ) {
            return Collections.emptyList();
        }
        return mAllMusicList;
    }

    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public synchronized void updateMusicArt(String musicId, Bitmap albumArt, Bitmap icon) {
        MediaMetadataCompat metadata = getMusic(musicId);
        metadata = new MediaMetadataCompat.Builder(metadata)

                // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                // example, on the lockscreen background when the media session is active.
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

                // set small version of the album art in the DISPLAY_ICON. This is used on
                // the MediaDescription and thus it should be small to be serialized if
                // necessary
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)

                .build();

        MutableMediaMetadata mutableMetadata = mMusicListById.get(musicId);
        if (mutableMetadata == null) {
            throw new IllegalStateException("Unexpected error: Inconsistent data structures in " +
                    "MusicProvider");
        }

        mutableMetadata.metadata = metadata;
    }

    /**
     * Get the list of music tracks from media store and caches the track information
     * for future reference, keying tracks by musicId and grouping by album.
     */
    public void retrieveMediaAsync(final Callback callback) {
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    //所有音乐
    private synchronized void buildAllMusicLists() {
        for (MutableMediaMetadata m : mMusicListById.values()) {
            if (mAllMusicList == null) {
                mAllMusicList = new ArrayList<>();
            }
            mAllMusicList.add(m.metadata);
        }
    }

    //专辑分类
    private synchronized void buildListsByAlbum() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByAlbum = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String album = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            List<MediaMetadataCompat> list = newMusicListByAlbum.get(album);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByAlbum.put(album, list);
            }
            list.add(m.metadata);
        }
        mMusicListByAlbum = newMusicListByAlbum;
    }

    //歌手分类
    private synchronized void buildListsBySinger() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListBySinger = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String singer = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            List<MediaMetadataCompat> list = newMusicListBySinger.get(singer);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListBySinger.put(singer, list);
            }
            list.add(m.metadata);
        }
        mMusicListBySinger = newMusicListBySinger;
    }

    //重新读取音乐资源
    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Iterator<MediaMetadataCompat> tracks = mSource.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }

                buildAllMusicLists();
                buildListsByAlbum();
                buildListsBySinger();
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }


    //获得子菜单
    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        //获得所有的歌曲
        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mediaId)) {
            for (MediaMetadataCompat metadata : getAllMusics()) {
                mediaItems.add(createMediaItem(metadata, null));
            }
        }
        //获得所有的专辑列表
        else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM.equals(mediaId)) {
            for (String album : getAlbums()) {
                mediaItems.add(createBrowsableMediaItemForGenre(album, resources,MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM));
            }
        }
        //获得所有的歌手列表
        else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER.equals(mediaId)) {
            for (String singer : getSingers()) {
                mediaItems.add(createBrowsableMediaItemForGenre(singer, resources,MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER));
            }
        }
        //获得某一专辑的所有歌曲
        else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)) {
            String album = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByAlbum(album)) {
                mediaItems.add(createMediaItem(metadata,MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM));
            }

        }
        //获得某一歌手的所有歌曲
        else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER)) {
            String singer = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsBySinger(singer)) {
                mediaItems.add(createMediaItem(metadata,MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER));
            }

        }else {
            Log.v(TAG, "Skipping unmatched mediaId: " + mediaId);
        }
        return mediaItems;
    }



    //创建音乐分类选项列表
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)
                .setTitle(resources.getString(R.string.browse_albums))
                .setSubtitle(resources.getString(R.string.browse_album_subtitle))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    //按照分类创建分类项目列表
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String value,Resources resources,String type) {
        //专辑
        if(type == MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM){
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, value))
                    .setTitle(value)
                    .setSubtitle(resources.getString(
                            R.string.browse_musics_by_album_subtitle, value))
                    .build();
            return new MediaBrowserCompat.MediaItem(description,
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
        }
        else{
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER, value))
                    .setTitle(value)
                    .setSubtitle(resources.getString(
                            R.string.browse_musics_by_singer_subtitle, value))
                    .build();
            return new MediaBrowserCompat.MediaItem(description,
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
        }

    }

    //按照分类后的项目创建可播放的音乐资源列表
    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String hierarchy) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)

        String hierarchyAwareMediaID ;
        if (hierarchy != null) {
            switch (hierarchy) {
                case MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                    String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
                    hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                            metadata.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, album);
                    break;
                case MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER:
                    String singer = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                    hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                            metadata.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER, singer);
                    break;
                default:
                    hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                            metadata.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_ROOT);
            }
        } else {
            hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                    metadata.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_ROOT);
        }
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }
}
