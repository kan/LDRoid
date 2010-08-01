package net.fushihara.LDRoid;

import net.fushihara.LDRoid.LDRClient.Feed;
import net.fushihara.LDRoid.LDRClient.Feeds;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FeedView extends Activity implements OnClickListener {
	private WebView webView;
	private Button prev_button;
	private Button next_button;
	private Button open_button;
	private String subscribe_title;
	private String subscribe_id;
	private Feeds feeds;
	private int feed_pos;
	private Button pin_button;
	private TextView title;
	private UnReadFeedsCache cache;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.feed_view);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.feed_view_title);
        
        prev_button = (Button) findViewById(R.id.PrevButton);
        prev_button.setOnClickListener(this);
        next_button = (Button) findViewById(R.id.NextButton);
        next_button.setOnClickListener(this);
        open_button = (Button) findViewById(R.id.OpenButton);
        open_button.setOnClickListener(this);
        pin_button = (Button) findViewById(R.id.PinButton);
        pin_button.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.web_view);
        title = (TextView) findViewById(R.id.title_text);
        
        subscribe_id = instance != null ? instance.getString(Main.KEY_SUBS_ID) : null;
        
        if (subscribe_id == null) {
        	Bundle extra = getIntent().getExtras();
        	subscribe_id = extra != null ? extra.getString(Main.KEY_SUBS_ID) : null;
        	subscribe_title = extra != null ? extra.getString(Main.KEY_SUBS_TITLE) : null;
        	title.setText(subscribe_title);
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
			title.setText("("+String.valueOf(feed_pos+1)+"/"+feeds.size()+")"+subscribe_title);
			
			if ( feed_pos + 1 == feeds.size() ) {
				TouchFeedTask task = new TouchFeedTask(title, getClient(), feeds.last_stored_on);
				task.execute(subscribe_id);
				
				Intent intent = new Intent();
				intent.putExtra(Main.KEY_SUBS_ID, subscribe_id);
				setResult(RESULT_OK, intent);
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
	}

	private Feed currentFeed() {
		if (feeds == null || feeds.size()==0) return null;
		return feeds.get(feed_pos);
	}
	
}
