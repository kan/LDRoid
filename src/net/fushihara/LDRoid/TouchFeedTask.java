package net.fushihara.LDRoid;

import android.os.AsyncTask;
import android.widget.TextView;

public class TouchFeedTask extends AsyncTask<String, Void, Void> {

	private LDRClient client;
	private TextView title;
	
	public TouchFeedTask(TextView title, LDRClient client) {
		this.client = client;
		this.title  = title;
	}
	
	@Override
	protected Void doInBackground(String... sub_ids) {
		client.touchAll(sub_ids[0]);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		title.setText("Šù“Ç‚É‚µ‚Ü‚µ‚½");
	}
}
