package yearsj.com.coolplayer.View.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import yearsj.com.coolplayer.View.adapter.MusicItemAdapter;
import yearsj.com.coolplayer.View.model.MusicItem;
import yearsj.com.coolplayer.View.ui.R;
import yearsj.com.coolplayer.View.util.LogHelper;
import yearsj.com.coolplayer.View.util.MediaIDHelper;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class ItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_MEDIA_ID = "media_id";
    // TODO: Customize parameters
    TextView mainTitile;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_item_list, container, false);

        MusicItemAdapter musicItemAdapter=new MusicItemAdapter(getActivity(),R.layout.two_item_list);

        ListView listView=(ListView) view.findViewById(R.id.musicListView);
        listView.setAdapter(musicItemAdapter);


        return view;
    }


    void initialView(){
        mainTitile=(TextView)view.findViewById(R.id.mainTitile);


    }

    public void changeTitle(CharSequence title){
        if (title == null) {
            title = getString(R.string.app_name);
        }
        mainTitile.setText(title);
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if(args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(ItemFragment.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }


}
