package net.fushihara.LDRoid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class SubsAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused")
	private static final String TAG = "SubsAdapter";
	
	public static final byte FLAG_PREFETCHED = 0x01;

	private static int [] rateColors;
	private static int title_normal;
	private static int title_empty;
	private static int title_prefetched;
	
	private SubscribeLocalList items;
	private LayoutInflater inflater;
	
	public SubsAdapter(Context context, SubscribeLocalList subs) {
		items = subs;
		
		inflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		if (items == null) {
			items = new SubscribeLocalList();
		}
		
		if (rateColors == null) {
	        Resources res = context.getResources();
	        rateColors = new int [] {
	        		res.getColor(R.color.rate0),
	        		res.getColor(R.color.rate1),
	        		res.getColor(R.color.rate2),
	        		res.getColor(R.color.rate3),
	        		res.getColor(R.color.rate4),
	        		res.getColor(R.color.rate5),
	        };
	        TypedArray a = context.obtainStyledAttributes(R.styleable.SubsAdapter);
	        title_normal = a.getColor(R.styleable.SubsAdapter_feedTitleColorNormal, 0xFFD7D7D7);
	        title_empty = a.getColor(R.styleable.SubsAdapter_feedTitleColorEmpty, 0xFF707070);
	        title_prefetched = a.getColor(R.styleable.SubsAdapter_feedTitleColorPrefetched, 0xFF00D7D7);
		}
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.subs_row, null);
		}
		SubscribeLocal s = items.get(position);
		
		TextView t = (TextView)view.findViewById(R.id.title);
		t.setText(s.getTitle());
		if (s.getUnreadCount() == 0) {
			t.setTextColor(title_empty);
		}
		else if (s.isPrefetched()) {
			t.setTextColor(title_prefetched);
		}
		else {
			t.setTextColor(title_normal);
		}
		
		t = (TextView)view.findViewById(R.id.count);
		t.setText(Integer.toString(s.getUnreadCount()));
		
		t = (TextView)view.findViewById(R.id.ratebar);
		int rate = s.getRate();
		if (rate >= 0 && rate <= 5) {
			t.setBackgroundColor(rateColors[rate]);
		}
		else {
			t.setBackgroundColor(rateColors[0]);
		}
		
		return view;
	}

}
