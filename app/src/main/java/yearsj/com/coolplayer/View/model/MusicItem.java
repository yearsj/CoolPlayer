package yearsj.com.coolplayer.View.model;

/**
 * Created by bing on 2016/6/12.
 */
public class MusicItem {
    String title;
    String info;

    int sourceId;

    public MusicItem(String title,String info){
        this.title=title;
        this.info=info;
    }

    public MusicItem(String title,String info,int sourceId){
        this.title=title;
        this.info=info;
        this.sourceId=sourceId;
    }

    public String getTitle(){
        return title;
    }

    public String getInfo(){
        return info;
    }

    public int getSourceId(){
        return sourceId;
    }
}
