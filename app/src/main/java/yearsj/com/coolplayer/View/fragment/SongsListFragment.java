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
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import yearsj.com.coolplayer.R;
import yearsj.com.coolplayer.View.adapter.SortAdapter;
import yearsj.com.coolplayer.View.model.SortModel;
import yearsj.com.coolplayer.View.ui.view.CharacterSideBarView;
import yearsj.com.coolplayer.View.util.CharacterParser;
import yearsj.com.coolplayer.View.util.PinyinComparator;


/**
 * Created by bing on 2016/6/2.
 */
public class SongsListFragment extends Fragment {
    /**
     * 事件列表
     **/
    private ListView list;
    private View view;
    LayoutInflater inflater;

    private SortAdapter adapter;
    private CharacterSideBarView sideBar;
    private TextView dialog;

    private PinyinComparator pinyinComparator;
    private CharacterParser characterParser;
    private List<SortModel> sourceDataList;
    private int listViewHeight,oneListHight;
    private List<String>  titles=new ArrayList<String>();


    final String TITLE = "title";
    final String INFO = "info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("yearsj", "fragment1-->onCreate()");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_songs, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
        initial();
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
//        if(listAdapter.getCount()!=0)
//        oneListHight= listItem.getMeasuredHeight()+listView.getDividerHeight();
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listViewHeight=getActivity().getWindowManager().getDefaultDisplay().getHeight()/3*2;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("yearsj", "fragment1-->onCreateView()");

        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
            Log.v("yearsj", "fragment1-->移除已存在的View");
        }

        return view;
    }

    void initial() {
        list = (ListView) view.findViewById(R.id.songListView);
        sideBar = (CharacterSideBarView) view.findViewById(R.id.sidebars);
        dialog = (TextView)view.findViewById(R.id.adialog);
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        loadData();
        setListViewHeightBasedOnChildren(list);
        setOnListListener();

        sideBar.setTextView(dialog, listViewHeight);
        sideBar.setOnTouchingLetterChangedListener(new CharacterSideBarView.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                final int position = adapter.getPositionForSection(s.charAt(0));
                System.out.println("position================"+position);
                Log.i("position",position+"");
                if (position != -1) {
                    list.requestFocus();
                    list.setItemChecked(position, true);
                    list.setSelectionFromTop(position,oneListHight);
                    list.smoothScrollToPosition(position);

                }

            }
        });
    }

    void loadData() {
        ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map;
        char aChar='a';

        for (int i = 0; i < 10; i++) {
            map = new HashMap<String, Object>();
            aChar=(char)(aChar + 1);
            String title=aChar+"陈奕迅";
            map.put(TITLE, title);
            map.put(INFO, "好久不见·认了吧");
            titles.add(title);
            mylist.add(map);
        }

        sourceDataList = filledData(titles);
        Collections.sort(sourceDataList, pinyinComparator);
        adapter = new SortAdapter(view.getContext(),
                mylist,
                R.layout.two_item_list,


                new String[]{ TITLE, INFO},


                new int[]{ R.id.titleView, R.id.infoView},sourceDataList);

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
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

     //   list.setAdapter(singerAdapter);
     list.setAdapter(adapter);

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


            }

        });

    }


    private List<SortModel> filledData(List<String> data) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < data.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(data.get(i));

            String pinyin = characterParser.getSelling(data.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }
            mSortList.add(sortModel);
        }
        return mSortList;
    }
}

