package net.fushihara.LDRoid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class GetIconTask extends AsyncTask<String, Void, Bitmap> {
	
	private ImageView iv;
	private static HashMap<String,Bitmap> cache = new HashMap<String,Bitmap>();
	
	GetIconTask(ImageView i) {
		this.iv = i;
	}

	@Override
	protected Bitmap doInBackground(String... uris) {
		try {
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
		if ( result != null ) {
			iv.setImageBitmap(result);
		}
	}
}
