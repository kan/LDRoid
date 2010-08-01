package net.fushihara.LDRoid;

import net.fushihara.LDRoid.LDRClient.Feeds;
import android.os.AsyncTask;
import android.util.Log;

public class PrefetchUnReadFeedsTask extends
		AsyncTask<String, Void, Feeds> {
	private static final String TAG = "PrefetchUnreadFeedsTask"; 
	public interface OnPrefetchUnReadFeedsListener {
		public void onPrefetchUnReadFeedsTaskComplete(Object sender, String subscribe_id, Feeds feeds, Exception e);
	}
	
	private LDRClient client;
	private Exception error;
	private OnPrefetchUnReadFeedsListener listener; 
	private String subscribe_id;
	
	public PrefetchUnReadFeedsTask(LDRClient client, OnPrefetchUnReadFeedsListener listener) {
		this.client = client;
		this.listener = listener;
	}
	
	@Override
	protected Feeds doInBackground(String... sub_ids) {
		try {
			subscribe_id = sub_ids[0];
			Log.d(TAG, "prefetch " + subscribe_id);
			return client.unRead(subscribe_id);
		}
		catch (Exception e) {
			e.printStackTrace();
			error = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute(Feeds result) {
		if (listener != null) {
			listener.onPrefetchUnReadFeedsTaskComplete(this, subscribe_id, result, error);
		}
	}

}
