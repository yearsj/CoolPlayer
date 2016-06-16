package yearsj.com.coolplayer.View.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import yearsj.com.coolplayer.View.model.SortModel;
import yearsj.com.coolplayer.View.ui.R;


public class SortAdapter extends SimpleAdapter implements SectionIndexer {
	private List<SortModel> list = null;
	private List<Map<String,String>> data;
	private Context mContext;

	final String TITLE = "title";
	final String INFO = "info";


//	public SortAdapter(Context mContext, List<SortModel> list) {
//		this.mContext = mContext;
//		this.list = list;
//	}

	public SortAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,List<SortModel> list) {
		super(context, data, resource, from, to);
		this.list=list;
		this.mContext=context;
		this.data=( List<Map<String,String>>)data;
	}


	public void clear() {
		this.data.clear();
		this.list.clear();
		notifyDataSetChanged();
	}

	public void addAll(List<Map<String,String>> data,List<SortModel> sourceDataList){
		this.data.addAll(data);
		this.list.addAll(sourceDataList);
		notifyDataSetChanged();
	}


	public void add(Map<String,String> data,List<SortModel> sourceDataList){
		this.data.add(data);
		this.list=sourceDataList;
		notifyDataSetChanged();
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final SortModel mContent = list.get(position);
		Map indexMap=data.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.two_item_list, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.titleView);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvInfo=(TextView)view.findViewById(R.id.infoView);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		int section = getSectionForPosition(position);

		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.GONE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		viewHolder.tvTitle.setText(indexMap.get(TITLE).toString());
		viewHolder.tvInfo.setText(indexMap.get(INFO).toString());
		return view;

	}

	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		TextView tvInfo;
	}


	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}


	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		return -1;
	}


	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}