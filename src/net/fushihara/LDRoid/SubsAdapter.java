package net.fushihara.LDRoid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.content.Context;
import android.content.res.Resources;
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
	
	private List<Subscribe> items;
	private HashMap<String,Byte> itemsFlag;
	private LayoutInflater inflater;
	private Context context;
	
	public SubsAdapter(Context context, List<Subscribe> subs) {
		this.context = context;
		items = subs;
		itemsFlag = new HashMap<String,Byte>();
		
		inflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		if (items == null) {
			items = new ArrayList<Subscribe>();
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
		}
	}
	
	public void setFlag(String subscribe_id, byte flag) {
		itemsFlag.put(subscribe_id, flag);
	}
	
	public byte getFlag(String subscribe_id) {
		Byte b = itemsFlag.get(subscribe_id);
		if (b == null) {
			return 0;
		}
		return b;
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
		Subscribe s = items.get(position);
		byte flag = getFlag(s.subscribe_id);
		
		TextView t = (TextView)view.findViewById(R.id.title);
		t.setText(s.title);
		if (s.unread_count == 0) {
			t.setTextAppearance(context, R.style.subs_title_empty);
		}
		else if ((flag & FLAG_PREFETCHED) != 0) {
			t.setTextAppearance(context, R.style.subs_title_prefetched);
		}
		else {
			t.setTextAppearance(context, R.style.subs_title);
		}
		
		t = (TextView)view.findViewById(R.id.count);
		t.setText(Integer.toString(s.unread_count));
		
		t = (TextView)view.findViewById(R.id.ratebar);
		if (s.rate >= 0 && s.rate <= 5) {
			t.setBackgroundColor(rateColors[s.rate]);
		}
		else {
			t.setBackgroundColor(rateColors[0]);
		}
		
		return view;
	}

}
