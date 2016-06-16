package yearsj.com.coolplayer.View.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import yearsj.com.coolplayer.View.service.MusicService;
import yearsj.com.coolplayer.View.ui.view.WaitDialog;

/**
 * Created by bing on 2016/6/2.
 */
public class FindLocalMusicActivity extends Activity{

    private Button findMusicButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_local_music);
        findMusicButton = (Button)findViewById(R.id.findLocalMusic);
        findMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void scanMusic(){
        WaitDialog.showDialogForLoading(this,"正在扫描",true);
        Intent intent = new Intent();
        intent.setClass(this, MusicService.class);
        stopService(intent);
        WaitDialog.hideDialogForLoading();
    }

    public void back(View view){
        super.finish();
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
    }
}
