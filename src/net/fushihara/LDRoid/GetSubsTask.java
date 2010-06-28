package net.fushihara.LDRoid;

import java.util.List;
import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetSubsTask extends AsyncTask<LDRClient, Void, List<Subscribe>> {

	private Main   view;
	private ProgressDialog progressDialog;

	public GetSubsTask(Main v) {
		view   = v;
	}

	@Override
	protected void onPreExecute() {
        progressDialog = new ProgressDialog(view);  
        progressDialog.setTitle("–¢“ÇƒŠƒXƒgŽæ“¾’†");  
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.show();  
		super.onPreExecute();
	}
	
	@Override
	protected List<Subscribe> doInBackground(LDRClient... clients) {
		return clients[0].subs(1);
	}
	
	@Override
	protected void onPostExecute(List<Subscribe> result) {
		progressDialog.dismiss();
		view.setSubs(result);
	}
}
