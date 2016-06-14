package yearsj.com.coolplayer.View.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import yearsj.com.coolplayer.View.ui.MainActivity;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.AlbumArtCache;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.StringHelper;

@SuppressLint("NewApi")
public class PlayingFragment extends Fragment{

	private ImageView lines;

	/**播放列表*/
	private View showPlayListLayout;
	private View  playListLayout;
	private ImageView mPlayPause;
	private TextView mTitle;
	private TextView mSubtitle;
	private ImageView mAlbumArt;
	private Button closeListButton;

	private String mArtUrl;

	QueueFragment playListFragment;

	static boolean  showList=false;

	private Drawable poster;
	/**
	 * 事件列表
	 **/
	//private ListView list;
	private View view;
	LayoutInflater inflater;

	final String STATE = "state";
	final String TITLE = "title";
	final String INFO = "info";
	private static final String TAG = LogHelper.makeLogTag(PlayingFragment.class.getSimpleName());

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
			Bundle savedInstanceState)  
	{  
		view = inflater.inflate(R.layout.fragment_playing, container, false);
		init();
		return view;  
	}

	void init(){
		lines= (ImageView) view.findViewById(R.id.playList);
		playListLayout=view.findViewById(R.id.playListLayout);
		showPlayListLayout=view.findViewById(R.id.fragment_play_list);
		closeListButton= (Button) view.findViewById(R.id.closeList);
	//	list = (ListView) view.findViewById(R.id.playLists);
		mTitle=(TextView)view.findViewById(R.id.musicName);
		mSubtitle=(TextView)view.findViewById(R.id.musicInfo);
		mAlbumArt=(ImageView)view.findViewById(R.id.poster);
		mPlayPause=(ImageView)view.findViewById(R.id.playMusic);

	//	loadData();
	//	setOnListListener();
		lines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPlayList();
			}
		});

		mPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MediaControllerCompat controller = ((FragmentActivity) getActivity()).getSupportMediaController();
				PlaybackStateCompat stateObj = controller.getPlaybackState();
				final  int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();
				LogHelper.d(TAG, "Button pressed, in state " + state);

				if (state == PlaybackStateCompat.STATE_PAUSED ||
						state == PlaybackStateCompat.STATE_STOPPED ||
						state == PlaybackStateCompat.STATE_NONE) {
					controller.getTransportControls().play();
				} else if (state == PlaybackStateCompat.STATE_PLAYING ||
						state == PlaybackStateCompat.STATE_BUFFERING ||
						state == PlaybackStateCompat.STATE_CONNECTING) {
					controller.getTransportControls().pause();
				}
			}
		});

		closeListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPlayList();
			}
		});
	}


	void subscribePlayList(){
		if(playListFragment==null)
			playListFragment=new QueueFragment();
		FragmentTransaction transaction = ((MainActivity)getActivity()).getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.playListFrame,playListFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}



	 void showPlayList(){
		Animation anim = null;
		if(!showList){
			this.showList=true;
			anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_bottom_to_up);
			showPlayListLayout.setAnimation(anim);
			playListLayout.setVisibility(View.VISIBLE);
			subscribePlayList();
		}else{
			this.showList=false;
			anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up_to_bottom);
			showPlayListLayout.setAnimation(anim);
			playListLayout.setVisibility(View.GONE);
		}
	}


//	void loadData() {
//		ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
//		HashMap<String, Object> map;
//
//		for (int i = 0; i < 5; i++) {
//			map = new HashMap<String, Object>();
//			map.put(STATE, this.getResources().getDrawable(R.drawable.playing));
//			map.put(TITLE, "陈奕迅");
//			map.put(INFO, "好久不见·认了吧");
//			mylist.add(map);
//		}
//
//		SimpleAdapter singerAdapter = new ChangeTextColorAdapter(view.getContext(),
//				mylist,
//				R.layout.two_item_with_img_list,
//
//
//				new String[]{STATE , TITLE, INFO},
//
//
//				new int[]{R.id.poster, R.id.titleView, R.id.infoView});
//
//		singerAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
//			@Override
//			public boolean setViewValue(View view, Object data,
//										String textRepresentation) {
//				if (view instanceof ImageView && data instanceof Drawable) {
//					ImageView iv = (ImageView) view;
//					iv.setImageDrawable((Drawable) data);
//					return true;
//				}
//				return false;
//			}
//		});
//
//		list.setAdapter(singerAdapter);
//	}
//
//
//	/**
//	 * 列表子项点击事件响应
//	 */
//	private void setOnListListener() {
//		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//									int position, long id) {
//				// TODO 自动生成的方法存根
//
////                System.out.println(id);
//			}
//
//		});
//
//	}

	public void onConnected() {
		MediaControllerCompat controller = ((FragmentActivity) getActivity())
				.getSupportMediaController();
		LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
		if (controller != null) {
			onMetadataChanged(controller.getMetadata());
			onPlaybackStateChanged(controller.getPlaybackState());
			controller.registerCallback(mCallback);
		}
	}

	private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
		@Override
		public void onPlaybackStateChanged(PlaybackStateCompat state) {
			LogHelper.d(TAG, "Received playback state change to state ", state.getState());
			PlayingFragment.this.onPlaybackStateChanged(state);
		}

		@Override
		public void onMetadataChanged(MediaMetadataCompat metadata) {
			if (metadata == null) {
				return;
			}
			LogHelper.d(TAG, "Received metadata state change to mediaId=",
					metadata.getDescription().getMediaId(),
					" song=", metadata.getDescription().getTitle());
			PlayingFragment.this.onMetadataChanged(metadata);
		}
	};

	private void onMetadataChanged(MediaMetadataCompat metadata) {
		LogHelper.d(TAG, "onMetadataChanged ", metadata);
		if (getActivity() == null) {
			LogHelper.w(TAG, "onMetadataChanged called when getActivity null," +
					"this should not happen if the callback was properly unregistered. Ignoring.");
			return;
		}
		if (metadata == null) {
			return;
		}

		mTitle.setText(StringHelper.getFormatTitle(metadata));
		mSubtitle.setText(metadata.getDescription().getSubtitle());


		String artUrl = null;
		if (metadata.getDescription().getIconUri() != null) {
			artUrl = metadata.getDescription().getIconUri().toString();
		}
		if (!TextUtils.equals(artUrl, mArtUrl)) {
			mArtUrl = artUrl;
			Bitmap art = metadata.getDescription().getIconBitmap();
			AlbumArtCache cache = AlbumArtCache.getInstance();
			if (art == null) {
				art = cache.getIconImage(mArtUrl);
			}
			if (art != null) {
				mAlbumArt.setImageBitmap(art);
			} else {
				cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
							@Override
							public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
								if (icon != null) {
									LogHelper.d(TAG, "album art icon of w=", icon.getWidth(),
											" h=", icon.getHeight());
									if (isAdded()) {
										mAlbumArt.setImageBitmap(icon);
									}
								}
							}
						}
				);
			}
		}


		Bitmap defaultArt = BitmapFactory.decodeResource(getResources(),
				R.mipmap.ic_launcher);
		if (isAdded() && artUrl == null) {
			mAlbumArt.setImageBitmap(defaultArt);
		}
	}

	private void onPlaybackStateChanged(PlaybackStateCompat state) {
		LogHelper.d(TAG, "onPlaybackStateChanged ", state);
		if (getActivity() == null) {
			LogHelper.w(TAG, "onPlaybackStateChanged called when getActivity null," +
					"this should not happen if the callback was properly unregistered. Ignoring.");
			return;
		}
		if (state == null) {
			return;
		}
		boolean enablePlay = false;
		switch (state.getState()) {
			case PlaybackStateCompat.STATE_PAUSED:
			case PlaybackStateCompat.STATE_STOPPED:
				enablePlay = true;
				break;
			case PlaybackStateCompat.STATE_ERROR:
				LogHelper.e(TAG, "error playbackstate: ", state.getErrorMessage());
				Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
				break;
		}

		if (enablePlay) {
			mPlayPause.setImageDrawable(
					ContextCompat.getDrawable(getActivity(), R.drawable.play_music));
		} else {
			mPlayPause.setImageDrawable(
					ContextCompat.getDrawable(getActivity(),  R.drawable.pause));
		}
	}


}
