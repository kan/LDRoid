package net.fushihara.LDRoid;

import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Feed;

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
	
	@SuppressWarnings("unchecked")
	public List<Feed> getFeeds(String subscribe_id) {
		ArrayList<Feed> feeds = null;
		try {
			feeds = (ArrayList<Feed>)get(subscribe_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
	}
	
	public void putFeeds(String subscribe_id, List<Feed> feeds) {
		put(subscribe_id, feeds);
	}
}
