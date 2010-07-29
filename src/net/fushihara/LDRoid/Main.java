package net.fushihara.LDRoid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends ListActivity {
	private static final String TAG = "Main";
    public static final String KEY_LOGIN_ID = "login_id";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SUBS_ID  = "subs_id";
    public static final String KEY_SUBS_TITLE = "subs_title";
    private static final String SUBS_FILE = "subs";

	private static final int MENU_RELOAD_ID  = 0;
	private static final int MENU_SETTING_ID = 1;

	private List<Subscribe> subs;
	private boolean isSubsSaved = false;
	private int [] rateColors;

	private LDRClient client;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources();
        rateColors = new int [] {
        		res.getColor(R.color.rate0),
        		res.getColor(R.color.rate1),
        		res.getColor(R.color.rate2),
        		res.getColor(R.color.rate3),
        		res.getColor(R.color.rate4),
        		res.getColor(R.color.rate5),
        };
        
        // 保存されている subs をセット
        setSubs(loadSubsFromFile());
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
	
	public void onGetSubsTaskCompleted(List<Subscribe> result, Exception error) {
		if (error != null) {
			Toast.makeText(this, 
					"ERROR: " + error.getMessage(), 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		if (result == null) {
			Toast.makeText(this, "no feed", Toast.LENGTH_LONG).show();
		}

		// 読み込んだ subs をファイルに書き出す
		saveSubsToFile((ArrayList<Subscribe>)subs);
		
		setSubs(result);
	}
	
	private void setSubs(List<Subscribe> newSubs) {
		
		subs = newSubs;
		
		SubsAdapter adapter = new SubsAdapter(subs);
		setListAdapter(adapter);
	}
	
	private LDRClientAccount getAccount() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		LDRClientAccount account = new LDRClientAccount(
				pref.getString("login_id", null),
				pref.getString("password", null)
			);
		return account;
	}
	
	@SuppressWarnings("unchecked")
	private List<Subscribe> loadSubsFromFile() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ArrayList<Subscribe> result = null;
		
		try {
			fis = openFileInput(SUBS_FILE);
			ois = new ObjectInputStream(fis);
			result = (ArrayList<Subscribe>)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "loadSubsFromFile " + (result != null));
		
		return result;
	}
	
	private void saveSubsToFile(List<Subscribe> subs) {
		Log.d(TAG, "saveSubsToFile");
		isSubsSaved = true;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(SUBS_FILE, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(subs);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (!isSubsSaved) {
			saveSubsToFile(subs);
			isSubsSaved = true;
		}
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
    
    private class SubsAdapter extends BaseAdapter {
    	private List<Subscribe> items;
    	private LayoutInflater inflater;
    	
    	public SubsAdapter(List<Subscribe> subs) {
    		items = subs;
    		inflater = (LayoutInflater)getSystemService(
    				Context.LAYOUT_INFLATER_SERVICE);
    		if (items == null) {
    			items = new ArrayList<Subscribe>();
    		}
    	}
    	
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.subs_row, null);
			}
			Subscribe s = items.get(position);
			
			TextView t = (TextView)view.findViewById(R.id.title);
			t.setText(s.title);
			
			t = (TextView)view.findViewById(R.id.count);
			t.setText(Integer.toString(s.unread_count));
			
			t = (TextView)view.findViewById(R.id.ratebar);
			if (s.rate >= 0 && s.rate <= 5) {
				t.setBackgroundColor(rateColors[s.rate]);
			}
			else {
				t.setBackgroundColor(rateColors[0]);
			}

			return view;
		}
    
    }
}
