package net.fushihara.LDRoid;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

public class TouchFeedTask extends AsyncTask<String, Void, Void> {

	private LDRClient client;
	private TextView title;
	private Exception error = null;
	
	public TouchFeedTask(TextView title, LDRClient client) {
		this.client = client;
		this.title  = title;
	}
	
	@Override
	protected Void doInBackground(String... sub_ids) {
		try {	
			client.touchAll(sub_ids[0]);
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
