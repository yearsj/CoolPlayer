package yearsj.com.coolplayer.View.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import yearsj.com.coolplayer.View.model.SortModel;
import yearsj.com.coolplayer.View.ui.R;

/**
 * Created by bing on 2016/6/3.
 */
public class BrowerAdapter extends SimpleAdapter {
    List<Map<String,Object>> data;

    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public BrowerAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.data=(List<Map<String,Object>>)data;
    }


    public void clear() {
        this.data.clear();
        notifyDataSetChanged();
    }

    public void add(Map<String,Object> item) {
        this.data.add(item);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position,View contentView,ViewGroup parent){
        View row = super.getView(position, contentView, parent);
        return row;
    }


}
