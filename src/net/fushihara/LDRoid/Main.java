package net.fushihara.LDRoid;

import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends ListActivity {
	private static final String TAG = "Main";
    public static final String KEY_LOGIN_ID = "login_id";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SUBS_ID  = "subs_id";
    public static final String KEY_SUBS_TITLE = "subs_title";

	private static final int MENU_RELOAD_ID  = 0;
	private static final int MENU_SETTING_ID = 1;

	private List<Subscribe> subs;

	private LDRClient client;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		//loadSubs();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	//loadSubs();
    }

	private void loadSubs() {
		Log.d(TAG, "loadSubs");
		if ( client == null ) {
		    LDRClientAccount account = getAccount();

		    if (account.isEmpty()) {
				// ID/PW未設定
 	        	Intent intent = new Intent(this, Setting.class);
	        	startActivity(intent);
	        	return;
			}
			client = new LDRClient(account);
		}

		GetSubsTask task = new GetSubsTask(this);
		task.execute(client);
	}
	
	public void setSubs(List<Subscribe> result, Exception error) {
		
		if (error != null) {
			Toast.makeText(this, 
					"ERROR: " + error.getMessage(), 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		if (result == null) {
			Toast.makeText(this, "no feed", Toast.LENGTH_LONG).show();
			return;
		}

		subs = result;

		List<String> items = new ArrayList<String>();
		for (Subscribe sub: subs) {
			items.add(sub.title + " (" + sub.unread_count + ")" );
		}
		
		ArrayAdapter<String> notes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
	    // ListActivityにアイテムリストをセットする
	    setListAdapter(notes);
	}

	private LDRClientAccount getAccount() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		LDRClientAccount account = new LDRClientAccount(
				pref.getString("login_id", null),
				pref.getString("password", null)
			);
		return account;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
 	    menu.add(0, MENU_RELOAD_ID, 0, R.string.menu_reload).setIcon(android.R.drawable.ic_menu_rotate);
 	    menu.add(0, MENU_SETTING_ID, 0, R.string.menu_setting).setIcon(android.R.drawable.ic_menu_manage);
	    return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case MENU_RELOAD_ID:
            loadSubs();
            break;
        case MENU_SETTING_ID:
        	Intent intent = new Intent(this, Setting.class);
        	startActivityForResult(intent, MENU_SETTING_ID);
        	break;
        }
        return ret;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case MENU_SETTING_ID:
    		// 設定画面から帰ってきたらアカウントが変更されていないか確認する
    		if (client != null) {
        		// アカウントが変更されていたら、新しいアカウントで再取得する
    			LDRClientAccount newAccount = getAccount();
    			if (!newAccount.equals(client.getAccount())) {
    				client = null;
    				loadSubs();
    			}
    		}
			
    		break;
    	}
    }
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, FeedView.class);
        Subscribe sub = subs.get(position);
        LDRClientAccount account = getAccount();
		i.putExtra(KEY_LOGIN_ID, account);
        i.putExtra(KEY_SUBS_ID, sub.subscribe_id);
        i.putExtra(KEY_SUBS_TITLE, sub.title);
        startActivity(i);
    }
}
