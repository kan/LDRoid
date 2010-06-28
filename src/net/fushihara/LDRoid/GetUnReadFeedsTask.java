package net.fushihara.LDRoid;

import java.util.List;
import net.fushihara.LDRoid.LDRClient.Feed;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetUnReadFeedsTask extends AsyncTask<String, Void, List<Feed>> {

	private LDRClient client;
	private FeedView view;
	private ProgressDialog progressDialog;
	
	public GetUnReadFeedsTask(FeedView view, LDRClient client) {
		this.view   = view;
		this.client = client;
	}
	
	@Override
	protected void onPreExecute() {
        progressDialog = new ProgressDialog(view);  
        progressDialog.setTitle("フィード読込中");  
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.show();  
		super.onPreExecute();
	}
	
	@Override
	protected List<Feed> doInBackground(String... sub_ids) {
		return client.unRead(sub_ids[0]);
	}

	@Override
	protected void onPostExecute(List<Feed> result) {
		progressDialog.dismiss();
		view.setFeeds(result);
	}
}
