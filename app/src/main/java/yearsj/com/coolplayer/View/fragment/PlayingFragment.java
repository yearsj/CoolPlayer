package yearsj.com.coolplayer.View.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import yearsj.com.coolplayer.View.adapter.ChangeTextColorAdapter;
import yearsj.com.coolplayer.View.ui.FindLocalMusicActivity;
import yearsj.com.coolplayer.View.ui.MainActivity;
import yearsj.com.coolplayer.View.ui.PlayActivity;
import yearsj.com.coolplayer.View.ui.R;

@SuppressLint("NewApi")
public class PlayingFragment extends Fragment{

	private ImageView lines;

	/**播放列表*/
	private View showPlayListLayout;
	private View  playListLayout;
	static boolean  showList=false;

	private Drawable poster;
	/**
	 * 事件列表
	 **/
	private ListView list;
	private View view;
	LayoutInflater inflater;

	final String STATE = "state";
	final String TITLE = "title";
	final String INFO = "info";

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
			Bundle savedInstanceState)  
	{  
		view = inflater.inflate(R.layout.fragment_playing, container, false);
		init();
		return view;  
	}

	void init(){
		lines= (ImageView) view.findViewById(R.id.playList);
		playListLayout=view.findViewById(R.id.playListLayout);
		showPlayListLayout=view.findViewById(R.id.fragment_play_list);
		list = (ListView) view.findViewById(R.id.playLists);
		loadData();
		setListViewHeightBasedOnChildren(list);
		setOnListListener();
		lines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPlayList();
			}
		});

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

	 void showPlayList(){
		Animation anim = null;
		if(!showList){
			this.showList=true;
			anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_bottom_to_up);
			showPlayListLayout.setAnimation(anim);
			playListLayout.setVisibility(View.VISIBLE);
		}else{
			this.showList=false;
			anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up_to_bottom);
			showPlayListLayout.setAnimation(anim);
			playListLayout.setVisibility(View.GONE);
		}
	}


	void loadData() {
		ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map;

		for (int i = 0; i < 5; i++) {
			map = new HashMap<String, Object>();
			map.put(STATE, this.getResources().getDrawable(R.drawable.playing));
			map.put(TITLE, "陈奕迅");
			map.put(INFO, "好久不见·认了吧");
			mylist.add(map);
		}

		SimpleAdapter singerAdapter = new ChangeTextColorAdapter(view.getContext(),
				mylist,
				R.layout.two_item_with_img_list,


				new String[]{STATE , TITLE, INFO},


				new int[]{R.id.poster, R.id.titleView, R.id.infoView});

		singerAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
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

		list.setAdapter(singerAdapter);
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
