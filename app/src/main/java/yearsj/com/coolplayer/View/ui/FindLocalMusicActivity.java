package yearsj.com.coolplayer.View.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by bing on 2016/6/2.
 */
public class FindLocalMusicActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_local_music);
    }

    public void back(View view){
        super.finish();
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
    }
}
