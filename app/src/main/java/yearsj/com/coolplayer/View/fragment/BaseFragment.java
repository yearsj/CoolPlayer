package yearsj.com.coolplayer.View.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by yearsj on 2016/6/5.
 */
public abstract class BaseFragment extends Fragment {

    //Fragment 是否显示
    protected boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 显示
     */
    protected void onVisible() {
        lazyLoad();
    }


    /**
     * 不显示
     */
    protected void onInvisible(){}


    /**
     * 子类需要继承并实现的方法
     */
    protected abstract void lazyLoad();
}
