package net.fushihara.LDRoid;

import net.fushihara.LDRoid.LDRClient.Feed;
import android.os.AsyncTask;
import android.widget.Toast;

public class SetPinTask extends AsyncTask<Feed, Void, String> {

	private FeedView  view;
	private LDRClient client;
	private Exception error = null;
	
	public SetPinTask(FeedView view, LDRClient client) {
		this.view   = view;
		this.client = client;
	}
	
	@Override
	protected String doInBackground(Feed... feeds) {
		try {
			client.pin_add(feeds[0]);
			return feeds[0].title;
		}
		catch (Exception e) {
			e.printStackTrace();
			error = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (error != null) {
			Toast.makeText(view, error.getMessage(), Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(view, "Åw"+result+"ÅxÇÉsÉìÇ…ìoò^ÇµÇ‹ÇµÇΩ", Toast.LENGTH_LONG).show();
		}
	}
}
