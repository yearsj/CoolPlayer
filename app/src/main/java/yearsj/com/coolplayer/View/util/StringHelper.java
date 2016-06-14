package yearsj.com.coolplayer.View.util;

import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by bing on 2016/6/14.
 */
public class StringHelper {

    public static String getFormatTitle(MediaMetadataCompat metadata){
        String initalTitle=metadata.getDescription().getTitle().toString();
        if(initalTitle.indexOf("(")!=-1)
            initalTitle=initalTitle.substring(0,initalTitle.indexOf("("));
        return initalTitle;
    }

    public static String getFormatBrowerByLength(String brower,int length){
        String title=brower.substring(brower.indexOf("/")+1);
        if(title.length()>length)
            title=title.substring(0,length)+"...";
        return title;
    }
}
