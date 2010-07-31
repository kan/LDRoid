package net.fushihara.LDRoid;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

public class TouchFeedTask extends AsyncTask<String, Void, Void> {

	private LDRClient client;
	private TextView title;
	private Exception error = null;
	private long timestamp;
	
	public TouchFeedTask(TextView title, LDRClient client, long timestamp) {
		this.client = client;
		this.title  = title;
		this.timestamp = timestamp;
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
		if (error != null) {
			Toast.makeText(title.getContext(),
					"ERROR: " + error.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		else {
			title.setText("Šù“Ç‚É‚µ‚Ü‚µ‚½");
		}
	}
}
