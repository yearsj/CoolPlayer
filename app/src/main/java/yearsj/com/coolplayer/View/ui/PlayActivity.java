package yearsj.com.coolplayer.View.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import yearsj.com.coolplayer.R;
import yearsj.com.coolplayer.View.MyFragmentAdapter;
import yearsj.com.coolplayer.View.fragment.AlbumFragment;
import yearsj.com.coolplayer.View.fragment.PlayListFragment;

/**
 * Created by yearsj on 2016/6/4.
 */
public class PlayActivity extends FragmentActivity implements View.OnClickListener{

    private ViewPager mViewPager;
    private List<Fragment> fragments;
    private View dot1;
    private View dot2;
    private ImageView list_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        init();
    }

    private void init(){
        mViewPager = (ViewPager)findViewById(R.id.viewpager_play);
        fragments = new ArrayList<Fragment>();
        fragments.add(new PlayListFragment());
        fragments.add(new AlbumFragment());
        mViewPager.setAdapter(new MyFragmentAdapter(this.getSupportFragmentManager(), fragments));
        mViewPager.setCurrentItem(1);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        dot1 = (View)findViewById(R.id.dot_1);
        dot2 = (View)findViewById(R.id.dot_2);
        list_play = (ImageView)findViewById(R.id.list_play);
        list_play.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.list_play:
                mViewPager.setCurrentItem(0);
                break;
        }
    }

    /**
     * 内部类----viewpager监听器
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onPageSelected(int arg0) {
            // TODO Auto-generated method stub
            changeDot(arg0);
        }
    }

    private void changeDot(int index){
        switch (index){
            case 0:
                ViewGroup.LayoutParams lp1 = dot1.getLayoutParams();
                lp1.width = 8;
                lp1.height = 8;
                dot1.setLayoutParams(lp1);
                ViewGroup.LayoutParams np1 = dot2.getLayoutParams();
                np1.width = 6;
                np1.height = 6;
                dot2.setLayoutParams(np1);
                break;
            case 1:
                ViewGroup.LayoutParams lp2 = dot2.getLayoutParams();
                lp2.width = 8;
                lp2.height = 8;
                dot2.setLayoutParams(lp2);
                ViewGroup.LayoutParams np2 = dot1.getLayoutParams();
                np2.width = 6;
                np2.height = 6;
                dot1.setLayoutParams(np2);
                break;
            default: break;
        }
    }
}
