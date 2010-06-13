package net.fushihara.LDRoid;

import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends ListActivity {
    public static final String KEY_SUBS_ID = "sub_id";

	private static final int MENU_RELOAD_ID  = 0;
	private static final int MENU_SETTING_ID = 1;

	private List<Subscribe> subs;

	private LDRClient client;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		loadSubs();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	loadSubs();
    }

	private void loadSubs() {
		List<String> items = new ArrayList<String>();

		if ( client == null ) {
			client = new LDRClient(this);
		}
		subs = client.subs(this, 1);
		
		if (subs == null) {
			if (client.getLoginId() == null) {
	        	Intent intent = new Intent(this, Setting.class);
	        	startActivity(intent);
			} else {
				Toast.makeText(this, "LDR connect faild", Toast.LENGTH_LONG).show();
			}
			return;
		}
		for (Subscribe sub: subs) {
			items.add(sub.title + " (" + sub.unread_count + ")" );
		}
		
		ArrayAdapter<String> notes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
	    // ListActivityにアイテムリストをセットする
	    setListAdapter(notes);
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
        	startActivity(intent);
        	break;
        }
        return ret;
    }
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, FeedView.class);
        Subscribe sub = subs.get(position);
        i.putExtra(KEY_SUBS_ID, sub.subscribe_id);
        System.out.println(sub.subscribe_id);
        startActivity(i);
    }
}
