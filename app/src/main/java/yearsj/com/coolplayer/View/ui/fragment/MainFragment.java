package yearsj.com.coolplayer.View.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yearsj.com.coolplayer.View.model.MediaFragmentListener;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment  implements MediaFragmentListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;

    ViewPager pager;
    SingerListFragment singerListFragment;
    SongsListFragment songsListFragment;
    AlbumListFragment albumListFragment;
    ArrayList<Fragment> fragmentsContainter;
    ArrayList<String>   titleContainer    = new ArrayList<String>();

    private TabLayout mTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view= inflater.inflate(R.layout.fragment_main, container, false);
        initialViewPager();
        return view;
    }

    protected void updateNavigationView(String mediaId) {
        switch (mediaId) {
            case MediaIDHelper.MEDIA_ID_ROOT:
                pager.setCurrentItem(0);
                break;
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_SINGER:
                pager.setCurrentItem(1);
                break;
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                pager.setCurrentItem(2);
                break;
            default:
                pager.setCurrentItem(0);
        }
    }


    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {

    }

    @Override
    public void setMainTitle(CharSequence title) {


    }

    @Override
    public void setNavigationItem(String mediaId) {

    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }


    void initialViewPager(){
        pager = (ViewPager) view.findViewById((R.id.viewpager));
        singerListFragment=new SingerListFragment();
        songsListFragment=new SongsListFragment();
        albumListFragment=new AlbumListFragment();
        fragmentsContainter = new ArrayList<Fragment>();
        fragmentsContainter.add(songsListFragment);
        fragmentsContainter.add(singerListFragment);
        fragmentsContainter.add(albumListFragment);

        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);

        //添加标签
        titleContainer.add(this.getResources().getString(R.string.songs));
        titleContainer.add(this.getResources().getString(R.string.singer));
        titleContainer.add(this.getResources().getString(R.string.album));

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(0)));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titleContainer.get(2)));

        MyViewPager myViewPager=new MyViewPager(getActivity().getSupportFragmentManager(),fragmentsContainter,titleContainer);
        pager.setAdapter(myViewPager);
        mTabLayout.setupWithViewPager(pager);//将TabLayout和ViewPager关联起来
        mTabLayout.setTabsFromPagerAdapter(myViewPager);
    }

    class MyViewPager extends FragmentPagerAdapter {
        private List<Fragment> mViewList;
        private List<String>  titleContainer;

        public MyViewPager(FragmentManager fm, List<Fragment> mViewList, List<String>  titleContainer){
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
