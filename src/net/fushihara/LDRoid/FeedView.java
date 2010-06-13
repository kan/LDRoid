package net.fushihara.LDRoid;

import java.util.List;

import net.fushihara.LDRoid.LDRClient.Feed;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class FeedView extends Activity implements OnClickListener {
	private WebView webView;
	private Button prev_button;
	private Button next_button;
	private Button open_button;
	private List<Feed> feeds;
	private int feed_pos;
	private Button pin_button;
	private LDRClient client;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.feed_view);
        
        prev_button = (Button) findViewById(R.id.PrevButton);
        prev_button.setOnClickListener(this);
        next_button = (Button) findViewById(R.id.NextButton);
        next_button.setOnClickListener(this);
        open_button = (Button) findViewById(R.id.OpenButton);
        open_button.setOnClickListener(this);
        pin_button = (Button) findViewById(R.id.PinButton);
        pin_button.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.web_view);
        
        String sub_id = instance != null ? instance.getString(Main.KEY_SUBS_ID) : null;
        
        if (sub_id == null) {
        	Bundle extra = getIntent().getExtras();
        	sub_id = extra != null ? extra.getString(Main.KEY_SUBS_ID) : null;
        }
                
        if ( client == null ) {
        	client = new LDRClient(this);
        }
        feeds = client.unRead(this, sub_id);
        feed_pos = 0;
        client.touchAll(this, sub_id);
        
        loadData();
    }

	private void loadData() {
		webView.loadDataWithBaseURL(currentFeed().link, currentFeed().body, "text/html", "utf-8","null");
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
			client.pin_add(this, currentFeed());
		}
	}

	private Feed currentFeed() {
		return feeds.get(feed_pos);
	}
}
