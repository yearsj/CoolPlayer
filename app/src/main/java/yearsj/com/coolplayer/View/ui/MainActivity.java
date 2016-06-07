package yearsj.com.coolplayer.View.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import yearsj.com.coolplayer.View.fragment.AlbumListFragment;
import yearsj.com.coolplayer.View.fragment.SingerListFragment;
import yearsj.com.coolplayer.View.fragment.SongsListFragment;

public class MainActivity extends FragmentActivity {
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
	//通过pagerTabStrip可以设置标题的属性
	private PagerTabStrip tabStrip;



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

		tabStrip = (PagerTabStrip) this.findViewById(R.id.tabstrip);
		//取消tab下面的长横线
		tabStrip.setDrawFullUnderline(false);
		//设置tab的背景色
		tabStrip.setBackgroundColor(this.getResources().getColor(R.color.white));
		//tabStrip.setTextColor(this.getResources().getColor(R.color.theme));
		//设置当前tab页签的下划线颜色
		tabStrip.setTabIndicatorColor(this.getResources().getColor(R.color.theme));



		//添加标签
		titleContainer.add(this.getResources().getString(R.string.songs));
		titleContainer.add(this.getResources().getString(R.string.singer));
		titleContainer.add(this.getResources().getString(R.string.album));



		pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

			//viewpager中的组件数量
			@Override
			public int getCount() {
				return fragmentsContainter.size();
			}

			@Override
			public CharSequence getPageTitle(int position) {
				// TODO Auto-generated method stub
				SpannableStringBuilder ssb = new SpannableStringBuilder(" "
						+ titleContainer.get(position)); // space added before text for
//				ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.theme));//字体颜色设置
//				ssb.setSpan(fcs, 1, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//设置字体颜色
				ssb.setSpan(new RelativeSizeSpan(1.2f), 1, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ssb;
			}

			@Override
			public Fragment getItem(int position) {
				return fragmentsContainter.get(position);
			}

		});

		pager.setCurrentItem(0);
	}
}
