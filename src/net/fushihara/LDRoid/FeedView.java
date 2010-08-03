package net.fushihara.LDRoid;

import net.fushihara.LDRoid.LDRClient.Feed;
import net.fushihara.LDRoid.LDRClient.Feeds;
import net.fushihara.LDRoid.TouchFeedTask.OnTouchFeedTaskListener;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class FeedView extends Activity implements OnClickListener, OnTouchFeedTaskListener {
	private static final String TAG = "FeedView";
	
	private WebView webView;
	private Button prev_button;
	private Button next_button;
	private Button open_button;
	private Button pin_button;
	private Button share_button;
	private String subscribe_title;
	private String subscribe_id;
	private Feeds feeds;
	private int feed_pos;
	private UnReadFeedsCache cache;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.feed_view);
        
        prev_button = (Button) findViewById(R.id.PrevButton);
        prev_button.setOnClickListener(this);
        next_button = (Button) findViewById(R.id.NextButton);
        next_button.setOnClickListener(this);
        open_button = (Button) findViewById(R.id.OpenButton);
        open_button.setOnClickListener(this);
        pin_button = (Button) findViewById(R.id.PinButton);
        pin_button.setOnClickListener(this);
        share_button = (Button) findViewById(R.id.ShareButton);
        share_button.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.web_view);
        
        webView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return processKey(keyCode, event);
			}
		});
        
        subscribe_id = instance != null ? instance.getString(Main.KEY_SUBS_ID) : null;
        
        if (subscribe_id == null) {
        	Bundle extra = getIntent().getExtras();
        	subscribe_id = extra != null ? extra.getString(Main.KEY_SUBS_ID) : null;
        	subscribe_title = extra != null ? extra.getString(Main.KEY_SUBS_TITLE) : null;
        	setTitle(subscribe_title);
        }
               
        cache = UnReadFeedsCache.getInstance(getApplicationContext());
        Feeds cached_feeds = cache.getFeeds(subscribe_id);
        if (cached_feeds != null) {
        	setFeeds(cached_feeds);
        }
        else {
	        GetUnReadFeedsTask task = new GetUnReadFeedsTask(this, getClient());
	        task.execute(subscribe_id);
        }
    }
    
    private LDRClient getClient() {
    	LDRoidApplication app = (LDRoidApplication)getApplication();
    	return app.getClient();
    }
    
    public void onGetUnReadFeedsTaskCompleted(Feeds result, Exception e) {
    	if (e != null) {
    		Toast.makeText(this, "ERROR: " + e.getMessage(), 
    				Toast.LENGTH_LONG).show();
    	}
    	else {
    		cache.putFeeds(subscribe_id, result);
    	}
    	setFeeds(result);
    }
    
    public void setFeeds(Feeds result) {
    	feeds = result;
    	feed_pos = 0;
    	
    	loadData();
    }
    
    private void updateButtons() {
    	if (feeds == null || feeds.size() == 0) {
    		prev_button.setEnabled(false);
    		next_button.setEnabled(false);
    	}
    	else {
    		prev_button.setEnabled(feed_pos > 0);
    		next_button.setEnabled(feed_pos < feeds.size()-1);
    	}
    	boolean isSelected = (currentFeed() != null); 
		pin_button.setEnabled(isSelected);
		open_button.setEnabled(isSelected);
    }

	private void loadData() {
		Feed feed = currentFeed();
		if (feed != null) {
			String body_html = "<h2>" + feed.title + "</h2>\n" + feed.body;
			webView.loadDataWithBaseURL(feed.link, body_html, "text/html", "utf-8","null");
			setTitle("("+String.valueOf(feed_pos+1)+"/"+feeds.size()+")"+subscribe_title);
			
			if ( feed_pos + 1 == feeds.size() ) {
				// 既読にするタスクを開始
				TouchFeedTask task = new TouchFeedTask(getClient(), 
						feeds.last_stored_on, this);
				task.execute(subscribe_id);
				
				setResult(RESULT_OK, getIntent());
			}
		}
		updateButtons();
	}

	@Override
	public void onClick(View v) {
		if ( v == prev_button ) {
			if ( feed_pos > 0 ) {
				feed_pos--;
				loadData();
			}
		}
		if ( v == next_button ) {
			if ( feed_pos < feeds.size()-1) {
				feed_pos++;
				loadData();
			}
		}
		if ( v == open_button ) {
			if (currentFeed() != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(currentFeed().link));
				startActivity(intent);
			}
		}
		if ( v == pin_button ) {
			if (currentFeed() != null) {
				SetPinTask task = new SetPinTask(this, getClient());
				task.execute(currentFeed());
			}
		}
		if ( v == share_button ) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_SUBJECT, currentFeed().title);
			intent.putExtra(Intent.EXTRA_TEXT, currentFeed().link);
			try {
				startActivity(Intent.createChooser(intent, null));
			} catch (ActivityNotFoundException  e) {
			}
		}
	}

	private Feed currentFeed() {
		if (feeds == null || feeds.size()==0) return null;
		return feeds.get(feed_pos);
	}

	private boolean processKey(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			Log.d(TAG, Integer.toString(keyCode));
			switch (keyCode) {
			case KeyEvent.KEYCODE_SPACE:
				if (event.isShiftPressed()) webView.pageUp(false);
				else webView.pageDown(false);
				return true;
			case KeyEvent.KEYCODE_J:
				onClick(next_button);
				return true;
			case KeyEvent.KEYCODE_K:
				onClick(prev_button);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onTouchFeedTaskComplete(Object sender, Exception e) {
		if (e != null) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, getText(R.string.toast_touched), Toast.LENGTH_SHORT).show();
		}
	}
	
}
