package yearsj.com.coolplayer.View.ui;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yearsj.com.coolplayer.View.model.MediaBrowserProvider;
import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.fragment.BrowerFragment;
import yearsj.com.coolplayer.View.ui.fragment.ItemFragment;
import yearsj.com.coolplayer.View.ui.fragment.MainFragment;
import yearsj.com.coolplayer.View.ui.fragment.PlayingFragment;
import yearsj.com.coolplayer.View.ui.fragment.SongsListFragment;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

public class MainActivity extends AppCompatActivity implements MediaBrowserProvider{

	private ItemFragment itemFragment;
	private MainFragment mainFragment;
	private PlayingFragment playingFragment;
	private MediaBrowserCompat mediaBrowserCompat;
	private FrameLayout mainContentLayout;

	private static final String TAG = LogHelper.makeLogTag(MainActivity.class.getSimpleName());
	private int sideBarHight;
	private final int addHight=200;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mediaBrowserCompat= new MediaBrowserCompat(this,
				new ComponentName(this, MusicService.class), mConnectionCallback, null);

		playingFragment = (PlayingFragment) getFragmentManager()
				.findFragmentById(R.id.id_fragment_playing);

		mainContentLayout= (FrameLayout)findViewById(R.id.mainContent);
		sideBarHight=this.getWindowManager().getDefaultDisplay().getHeight()/3*2;

	}

	@Override
	protected void onStart() {
		super.onStart();
		hidePlaybackControls();
		mediaBrowserCompat.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (getSupportMediaController() != null) {
			getSupportMediaController().unregisterCallback(mMediaControllerCallback);
		}
		mediaBrowserCompat.disconnect();
	}

	private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
			new MediaBrowserCompat.ConnectionCallback() {
				@Override
				public void onConnected() {
					LogHelper.d(TAG, "Music service connected");
					try {
						connectToSession(mediaBrowserCompat.getSessionToken());
					} catch (RemoteException e) {
						LogHelper.e(TAG, e, "could not connect media controller");
					}
				}
			};

	private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
		MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
		setSupportMediaController(mediaController);
		mediaController.registerCallback(mMediaControllerCallback);

		showMainContent();

		if (playingFragment != null) {
			playingFragment.onConnected();
		}
	}

	private final MediaControllerCompat.Callback mMediaControllerCallback =
			new MediaControllerCompat.Callback() {
				@Override
				public void onPlaybackStateChanged(PlaybackStateCompat state) {
					if (shouldShowControls()) {
						showPlaybackControls();
					} else {
						LogHelper.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
								"hiding controls because state is ", state.getState());
						hidePlaybackControls();
					}
				}

				@Override
				public void onMetadataChanged(MediaMetadataCompat metadata) {
					if (shouldShowControls()) {
						showPlaybackControls();
					} else {
						LogHelper.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
								"hiding controls because metadata is null");
						hidePlaybackControls();
					}
				}
			};

	protected boolean shouldShowControls() {
		MediaControllerCompat mediaController = getSupportMediaController();
		if (mediaController == null ||
				mediaController.getMetadata() == null ||
				mediaController.getPlaybackState() == null) {
			return false;
		}
		switch (mediaController.getPlaybackState().getState()) {
			case PlaybackStateCompat.STATE_ERROR:
			case PlaybackStateCompat.STATE_NONE:
			case PlaybackStateCompat.STATE_STOPPED:
				return false;
			default:
				return true;
		}
	}


	public void showPlayInfo(View v){
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, PlayActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_bottom_to_up, R.anim.just_stay);
	}

	/**
	 * 选中某个音乐播放
	 * @param item
     */

	public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
		LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
		if (item.isPlayable()) {
			getSupportMediaController().getTransportControls()
					.playFromMediaId(item.getMediaId(), null);
		} else if (item.isBrowsable()) {
			navigateToBrowser(item.getMediaId());
		} else {
			LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
					"mediaId=", item.getMediaId());
		}
	}

	protected void showPlaybackControls() {
		LogHelper.d(TAG, "showPlaybackControls");
		ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(mainContentLayout.getLayoutParams());
		margin.setMargins(0,0, margin.width, margin.height+addHight);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
		mainContentLayout.setLayoutParams(layoutParams);

		SongsListFragment.setSideBarHight(sideBarHight);

		getFragmentManager().beginTransaction()
				.show(playingFragment)
				.commit();
		overridePendingTransition(R.anim.slide_bottom_to_up, R.anim.just_stay);
	}

	protected void hidePlaybackControls() {
		LogHelper.d(TAG, "hidePlaybackControls");
		ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(mainContentLayout.getLayoutParams());
		margin.setMargins(0,0, margin.width, margin.height-addHight);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
		mainContentLayout.setLayoutParams(layoutParams);

		SongsListFragment.setSideBarHight(addHight+sideBarHight);

		getFragmentManager().beginTransaction()
				.hide(playingFragment)
				.commit();
		overridePendingTransition(R.anim.slide_bottom_to_up, R.anim.just_stay);
	}

	public void navigateToBrowser(String mediaId) {
		itemFragment = ItemFragment.newInstance(mediaId);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.mainContent, itemFragment);
		overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
		transaction.addToBackStack(null);
		transaction.commit();
	}



	public void showMainContent(){
		if(mainFragment==null)
			mainFragment = new MainFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.mainContent, mainFragment);
		overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public MediaBrowserCompat getMediaBrowser() {
		return mediaBrowserCompat;
	}
}
