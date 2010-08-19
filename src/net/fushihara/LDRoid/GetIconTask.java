package net.fushihara.LDRoid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class GetIconTask extends AsyncTask<String, Void, Bitmap> {
	
	private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();
	
	public interface OnGetIconListener {
		public void onGetIconTaskComplete(Object sender, String uri, Bitmap result);
	}
	
	private OnGetIconListener listener;
	private String uri;

	GetIconTask(OnGetIconListener listener) {
		this.listener = listener;
	}

	@Override
	protected Bitmap doInBackground(String... uris) {
		try {
			uri = uris[0];
			
			if(cache.containsKey(uris[0])) {
				return cache.get(uris[0]);
			}
			URL url = new URL(uris[0]);
			InputStream is = url.openStream();
			Bitmap bmp = BitmapFactory.decodeStream(is);
			cache.put(uris[0], bmp);
			return bmp;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		if (listener != null) {
			listener.onGetIconTaskComplete(this, uri, result);
		}
	}
	
	// ÉLÉÉÉbÉVÉÖçœÇ›ÇÃBitmapÇéÊìæ
	public static Bitmap getCache(String uri) {
		if(cache.containsKey(uri)) {
			return cache.get(uri);
		}
		return null;
	}
}
