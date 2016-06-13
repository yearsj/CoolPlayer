package yearsj.com.coolplayer.View.ui;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yearsj.com.coolplayer.View.model.MediaFragmentListener;
import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.fragment.AlbumListFragment;
import yearsj.com.coolplayer.View.ui.fragment.ItemFragment;
import yearsj.com.coolplayer.View.ui.fragment.MainFragment;
import yearsj.com.coolplayer.View.ui.fragment.PlayingFragment;
import yearsj.com.coolplayer.View.ui.fragment.SingerListFragment;
import yearsj.com.coolplayer.View.ui.fragment.SongsListFragment;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

public class MainActivity extends ExtendBaseActivity implements MediaFragmentListener{
	/**弹出菜单*/
	private PopupMenu popupMenu;
	/**菜单*/
	private Menu menu;
	private TextView mainTitile;

	private ItemFragment itemFragment;
	private MainFragment mainFragment;


	private static final String TAG = LogHelper.makeLogTag(MainActivity.class.getSimpleName());
	private static final String FRAGMENT_TAG = "mp_list_container";
	private static final String SAVED_MEDIA_ID="yearsj.com.coolplayer.View.MEDIA_ID";


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		inite();
	//	initializeFromParams(savedInstanceState,getIntent());
	}

	void inite(){
		mainTitile= (TextView) this.findViewById(R.id.mainTitile);
		initialMenu();
	}

	/**
	 *
	 * 初始化菜单
	 */
	@SuppressLint("NewApi")
	void initialMenu(){
		popupMenu = new PopupMenu(this, findViewById(R.id.menu));
		menu = popupMenu.getMenu();

		//通过XML导入菜单栏
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);

		// 设置监听事件
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				switch (item.getItemId()) {
					case R.id.findByTime:

						break;
					case R.id.findByName:

						break;
					case R.id.findLocalMusic:
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, FindLocalMusicActivity.class);
						startActivity(intent);
						overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	public void popupmenu(View v) {
		popupMenu.show();
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
	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
		LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
		if (item.isPlayable()) {
			getSupportMediaController().getTransportControls()
					.playFromMediaId(item.getMediaId(), null);
		} else if (item.isBrowsable()) {
			navigateToBrowser(item.getMediaId(), true);
		} else {
			LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
					"mediaId=", item.getMediaId());
		}
	}

	/**
	 * 设置主标题
	 * @param title
     */
	@Override
	public void setMainTitle(CharSequence title) {
		LogHelper.d(TAG, "Setting toolbar title to ", title);
		if(itemFragment==null)
			itemFragment=new ItemFragment();
		itemFragment.changeTitle(title);
	}


	/**
	 *
	 * @param mediaId
     */
	@Override
	public void setNavigationItem(String mediaId) {
		LogHelper.d(TAG, "Setting navigation view mediaId to ", mediaId);
		if (mediaId != null) {
			if(mainFragment==null)
				mainFragment=new MainFragment();
			mainFragment.setNavigationItem(mediaId);
		}
	}



	public void navigateToBrowser(String mediaId, boolean addToBackstack) {
		ItemFragment fragment = getBrowseFragment();

		if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
			fragment = new ItemFragment();
			fragment.setMediaId(mediaId);
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.setCustomAnimations(
					R.animator.slide_in_right, R.animator.slide_out_left,
					R.animator.slide_in_left, R.animator.slide_out_right);
			transaction.replace(R.id.startViewCon, fragment, FRAGMENT_TAG);
			if (addToBackstack) {
				transaction.addToBackStack(null);
			}
			transaction.commit();
		}
	}

	private ItemFragment getBrowseFragment() {
		return (ItemFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
	}

	private void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		String mediaId = null;
		if (savedInstanceState != null) {
			// If there is a saved media ID, use it
			mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
		} else if (intent.hasExtra(SAVED_MEDIA_ID)) {
			mediaId = intent.getExtras().getString(SAVED_MEDIA_ID);
		}
		navigateToBrowser(mediaId, false);
		setNavigationItem(mediaId);
	}

}
