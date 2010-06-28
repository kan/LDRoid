package net.fushihara.LDRoid;

import java.util.List;

import net.fushihara.LDRoid.LDRClient.Feed;
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

public class FeedView extends Activity implements OnClickListener {
	private WebView webView;
	private Button prev_button;
	private Button next_button;
	private Button open_button;
	private String subscribe_title;
	private String subscribe_id;
	private List<Feed> feeds;
	private int feed_pos;
	private Button pin_button;
	private LDRClient client;
	private TextView title;

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
        }
                
        if ( client == null ) {
        	Bundle extra = getIntent().getExtras();
        	LDRClientAccount account = new LDRClientAccount();
			account .login_id = extra != null ? extra.getString(Main.KEY_LOGIN_ID) : null;
        	account.password = extra != null ? extra.getString(Main.KEY_PASSWORD) : null;
        	client = new LDRClient(account);
        }
        
        GetUnReadFeedsTask task = new GetUnReadFeedsTask(this, client);
        task.execute(subscribe_id);
    }
    
    public void setFeeds(List<Feed> result) {
    	feeds = result;
    	feed_pos = 0;
    	loadData();
    }

	private void loadData() {
		String body_html = "<h2>" + currentFeed().title + "</h2>\n" + currentFeed().body;
		webView.loadDataWithBaseURL(currentFeed().link, body_html, "text/html", "utf-8","null");
		title.setText("("+String.valueOf(feed_pos+1)+"/"+feeds.size()+")"+subscribe_title);
		
		if ( feed_pos + 1 == feeds.size() ) {
			TouchFeedTask task = new TouchFeedTask(title, client);
			task.execute(subscribe_id);
		}
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
			Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(currentFeed().link));
			startActivity(intent);
		}
		if ( v == pin_button ) {
			SetPinTask task = new SetPinTask(this, client);
			task.execute(currentFeed());
		}
	}

	private Feed currentFeed() {
		return feeds.get(feed_pos);
	}
}
