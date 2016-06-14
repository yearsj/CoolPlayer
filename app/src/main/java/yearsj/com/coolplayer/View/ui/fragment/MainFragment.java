package yearsj.com.coolplayer.View.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import yearsj.com.coolplayer.View.ui.FindLocalMusicActivity;
import yearsj.com.coolplayer.View.ui.MainActivity;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

/**
 * Created by bing on 2016/6/13.
 */
public class MainFragment extends Fragment{
    /**弹出菜单*/
    private PopupMenu popupMenu;
    /**菜单*/
    private Menu menu;
    private ImageView menuImage;

    ViewPager pager;
    View view;

    ArrayList<android.support.v4.app.Fragment> fragmentsContainter;
    ArrayList<String>   titleContainer    = new ArrayList<String>();

    private TabLayout mTabLayout;

    FragmentManager fm;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("yearsj", "fragment1-->onCreate()");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_main, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
        initial();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return view;
    }


    public void initial(){
        menuImage=(ImageView) view.findViewById(R.id.menu);
        initialMenu();
        initialViewPager();

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    /**
     *
     * 初始化菜单
     */
    @SuppressLint("NewApi")
    void initialMenu(){
        popupMenu = new PopupMenu(view.getContext(), view.findViewById(R.id.menu));
        menu = popupMenu.getMenu();

        //通过XML导入菜单栏
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        // 设置监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.findLocalMusic:
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), FindLocalMusicActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }


    public void initialViewPager(){
        pager = (ViewPager) view.findViewById((R.id.viewpager));
        fragmentsContainter = new ArrayList<android.support.v4.app.Fragment>();
        fragmentsContainter.add(SongsListFragment.newInstance(MediaIDHelper.MEDIA_ID_ROOT));
        fragmentsContainter.add(BrowerFragment.newInstance(MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER));
        fragmentsContainter.add(BrowerFragment.newInstance(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM));

        mTabLayout = (TabLayout)view.findViewById(R.id.tabs);

        //添加标签
        titleContainer.add(this.getResources().getString(R.string.songs));
        titleContainer.add(this.getResources().getString(R.string.singer));
        titleContainer.add(this.getResources().getString(R.string.album));

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(0)));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(2)));

        MyViewPager myViewPager=new MyViewPager(((MainActivity)getActivity()).getSupportFragmentManager(),fragmentsContainter,titleContainer);
        pager.setAdapter(myViewPager);
        mTabLayout.setupWithViewPager(pager);//将TabLayout和ViewPager关联起来
        mTabLayout.setTabsFromPagerAdapter(myViewPager);
    }

    class MyViewPager extends FragmentPagerAdapter {
        private List<android.support.v4.app.Fragment> mViewList;
        private List<String>  titleContainer;

        public MyViewPager(FragmentManager fm, List<android.support.v4.app.Fragment> mViewList, List<String>  titleContainer){
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
        public android.support.v4.app.Fragment getItem(int position) {
            return mViewList.get(position);
        }

    }
}
