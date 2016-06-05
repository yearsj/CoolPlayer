package yearsj.com.coolplayer.View;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


/**
 * Created by yearsj on 2016/6/5.
 */
public class MyFragmentAdapter extends FragmentPagerAdapter {
    List<Fragment> list;
    public MyFragmentAdapter(FragmentManager fm, List<Fragment> list){
        super(fm);
        this.list = list;
    }
    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return list.get(arg0);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }
}