package net.fushihara.LDRoid;

import android.os.AsyncTask;

public class TouchFeedTask extends AsyncTask<String, Void, Void> {
	
	public interface OnTouchFeedTaskListener {
		public void onTouchFeedTaskComplete(Object sender, Exception e);
	}

	private LDRClient client;
	private Exception error;
	private long timestamp;
	private OnTouchFeedTaskListener listener; 
	
	public TouchFeedTask(LDRClient client, long timestamp, OnTouchFeedTaskListener listener) {
		this.client = client;
		this.timestamp = timestamp;
		this.listener = listener;
	}
	
	@Override
	protected Void doInBackground(String... sub_ids) {
		try {	
			client.touch(sub_ids[0], timestamp);
		}
		catch (Exception e) {
			e.printStackTrace();
			error = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (listener != null) {
			listener.onTouchFeedTaskComplete(this, error);
		}
	}
}
