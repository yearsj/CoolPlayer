package yearsj.com.coolplayer.View.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import yearsj.com.coolplayer.View.fragment.AlbumListFragment;
import yearsj.com.coolplayer.View.fragment.PlayingFragment;
import yearsj.com.coolplayer.View.fragment.SingerListFragment;
import yearsj.com.coolplayer.View.fragment.SongsListFragment;

public class MainActivity extends FragmentActivity{
	/**弹出菜单*/
	private PopupMenu popupMenu;
	/**菜单*/
	private Menu menu;

	ViewPager pager;
	SingerListFragment singerListFragment;
	SongsListFragment songsListFragment;
	AlbumListFragment albumListFragment;
	ArrayList<Fragment> fragmentsContainter;
	//标题列表
	ArrayList<String>   titleContainer    = new ArrayList<String>();

	private TabLayout mTabLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		inite();
	}

	void inite(){
		initialMenu();
		initialViewPager();
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


	void initialViewPager(){
		pager = (ViewPager) this.findViewById((R.id.viewpager));
		singerListFragment=new SingerListFragment();
		songsListFragment=new SongsListFragment();
		albumListFragment=new AlbumListFragment();
		fragmentsContainter = new ArrayList<Fragment>();
		fragmentsContainter.add(songsListFragment);
		fragmentsContainter.add(singerListFragment);
		fragmentsContainter.add(albumListFragment);

		mTabLayout = (TabLayout) findViewById(R.id.tabs);

		//添加标签
		titleContainer.add(this.getResources().getString(R.string.songs));
		titleContainer.add(this.getResources().getString(R.string.singer));
		titleContainer.add(this.getResources().getString(R.string.album));

		mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
		mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(0)));//添加tab选项卡
		mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(1)));
		mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(2)));

		MyViewPager myViewPager=new MyViewPager(getSupportFragmentManager(),fragmentsContainter,titleContainer);
		pager.setAdapter(myViewPager);
		mTabLayout.setupWithViewPager(pager);//将TabLayout和ViewPager关联起来
		mTabLayout.setTabsFromPagerAdapter(myViewPager);
	}


	public void showPlayInfo(View v){
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, PlayActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_bottom_to_up, R.anim.just_stay);
	}

	class MyViewPager extends FragmentPagerAdapter{
		private List<Fragment> mViewList;
		private List<String>  titleContainer;

		public MyViewPager(FragmentManager fm,List<Fragment> mViewList,List<String>  titleContainer){
			super(fm);
			this.mViewList=mViewList;
			this.titleContainer=titleContainer;
		}

		//viewpager中的组件数量
		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
		return titleContainer.get(position);
		}

		@Override
		public Fragment getItem(int position) {
			return mViewList.get(position);
		}

	}
}
