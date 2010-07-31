package net.fushihara.LDRoid;

import net.fushihara.LDRoid.LDRClient.Feeds;
import android.content.Context;

public class UnReadFeedsCache extends ObjToFile {
	private static UnReadFeedsCache inst;
	private static final String prefix = "urfc_";
	
	public static UnReadFeedsCache getInstance(Context context) {
		if (inst == null) {
			inst = new UnReadFeedsCache(context);
		}
		return inst;
	}
	
	private UnReadFeedsCache(Context context)  {
		super(context, prefix);
	}
	
	public Feeds getFeeds(String subscribe_id) {
		Feeds feeds = null;
		try {
			feeds = (Feeds)get(subscribe_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
	}
	
	public void putFeeds(String subscribe_id, Feeds feeds) {
		put(subscribe_id, feeds);
	}
}
