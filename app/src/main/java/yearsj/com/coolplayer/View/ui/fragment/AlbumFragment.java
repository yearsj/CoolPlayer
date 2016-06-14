package yearsj.com.coolplayer.View.ui.fragment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import yearsj.com.coolplayer.View.model.MediaDescriptionInfo;
import yearsj.com.coolplayer.View.ui.R;

/**
 * Created by yearsj on 2016/6/5.
 */
public class AlbumFragment extends BaseFragment {
    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    private ImageView albumCover;
    private View view = null;
    private TextView title;
    private TextView author;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_cover, container, false);
        albumCover = (ImageView)view.findViewById(R.id.albumcover);
        title = (TextView)view.findViewById(R.id.title_play);
        author=(TextView)view.findViewById(R.id.author_paly);
        isPrepared = true;
        mHasLoadedOnce = false;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        lazyLoad();
    }

    //刷新界面
    private void refreshData() {
        MediaDescriptionInfo mediaDescriptionInfo = (MediaDescriptionInfo)getActivity();
        Bitmap album = mediaDescriptionInfo.getCurrentMediaBitmap();
        MediaDescriptionCompat descriptionCompat = mediaDescriptionInfo.getCurrentMediaDescription();
        if(null!=album){
            albumCover.setImageBitmap(album);
        }
        if(null!= descriptionCompat){
            title.setText(descriptionCompat.getTitle());
            author.setText("--"+descriptionCompat.getSubtitle()+"--");
        }
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //显示加载进度对话框
                System.out.println("正在加载...");
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if (isSuccess) {
                    // 加载成功
                    refreshData();
                    mHasLoadedOnce = true;
                } else {
                    // 加载失败
                    Log.i("DailyFragment", "加载失败");
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return true;
            }
        }.execute();
    }

}
