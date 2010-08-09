package net.fushihara.LDRoid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;

// Subscribeに存在しないデータを扱うclass
// List<Subscribe> は基本的に変更しないデータとして扱い、
// 変更のあるデータはこちらで管理する
class SubscribeLocal implements Serializable {
	private static final long serialVersionUID = 4043669094720118571L;

	public static final byte TOUCH_DEFAULT = 0x00;
	public static final byte TOUCH_EXECUTING = 0x01;
	public static final byte TOUCH_FINISHED= 0x02;

	private String subscribe_id;
	private byte touch_state;
	private boolean is_prefetched;
	private transient Subscribe sub;
	
	public SubscribeLocal(Subscribe sub) {
		this.subscribe_id = sub.subscribe_id;
		this.touch_state = TOUCH_DEFAULT;
		this.sub = sub;
	}
	
	public boolean isTouched() {
		return touch_state != TOUCH_DEFAULT;
	}
	
	public void setTouchState(Byte value) {
		touch_state = value;
	}
	
	public void setSubscribe(Subscribe sub) {
		this.sub = sub;
	}
	
	public String getTitle() {
		return sub.title;
	}
	
	public String getIcon() {
		return sub.icon;
	}

	public String getSubscribeId() {
		return subscribe_id;
	}
	
	public int getUnreadCount() {
		if (isTouched()) return 0;
		return sub.unread_count;
	}
	
	public int getRate() {
		return sub.rate;
	}
	
	public boolean isPrefetched() {
		return is_prefetched;
	}
	
	public void setPrefetched(boolean value) {
		is_prefetched = value;
	}
}

class SubscribeLocalList extends ArrayList<SubscribeLocal>
{
	private static final long serialVersionUID = 589931431742210074L;

	public SubscribeLocalList() {
	}
	
	// List<Subscribe> から生成
	public SubscribeLocalList(List<Subscribe> subs) {
		int subs_size = subs.size();
		for (int j=0; j<subs_size; j++) {
			add(new SubscribeLocal(subs.get(j)));
		}
	}
	
	// List<Subscribe> と　SubscribeLocalListをマージして生成
	public SubscribeLocalList(List<Subscribe> subs, SubscribeLocalList sll) {
		int subs_size = subs.size();
		for (int j=0; j<subs_size; j++) {
			SubscribeLocal sl = sll.getItemById(subs.get(j).subscribe_id);
			if (sl != null) {
				sl.setSubscribe(subs.get(j));
				add(sl);
			}
			else {
				add(new SubscribeLocal(subs.get(j)));
			}
		}
	}
	
	SubscribeLocal getItemById(String subscribe_id) {
		int size = size();
		for (int j=0; j<size; j++) {
			if (subscribe_id.equals(get(j).getSubscribeId())) {
				return get(j);
			}
		}
		return null;
	}
}