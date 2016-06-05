package yearsj.com.coolplayer.View.fragment;

import  android.support.v4.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import yearsj.com.coolplayer.R;

public class AlbumListFragment extends Fragment {
    private Drawable poster;
    /**
     * 事件列表
     **/
    private ListView list;
    private View view;
    LayoutInflater inflater;

    final String POSTER = "poster";
    final String TITLE = "title";
    final String INFO = "info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("yearsj", "album-->onCreate()");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_album, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
        initial();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("yearsj", "album-->onCreateView()");

        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
            Log.v("yearsj", "fragment1-->移除已存在的View");
        }

        return view;
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    void initial() {
        list = (ListView) view.findViewById(R.id.albumListView);
        loadData();
        setListViewHeightBasedOnChildren(list);
        setOnListListener();
    }

    void loadData() {
        ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map;
        for (int i = 0; i < 5; i++) {
            map = new HashMap<String, Object>();
            map.put(POSTER, this.getResources().getDrawable(R.drawable.poster));
            map.put(TITLE, "陈奕迅");
            map.put(INFO, "好久不见·认了吧");
            mylist.add(map);
        }

        SimpleAdapter albumAdapter = new SimpleAdapter(view.getContext(),
                mylist,
                R.layout.two_item_with_img_list,


                new String[]{POSTER, TITLE, INFO},


                new int[]{R.id.poster, R.id.titleView, R.id.infoView});

        albumAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                }
                return false;
            }
        });

        list.setAdapter(albumAdapter);


    }


    /**
     * 列表子项点击事件响应
     */
    private void setOnListListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO 自动生成的方法存根

//                System.out.println(id);
            }

        });

    }
}
