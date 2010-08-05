package net.fushihara.LDRoid;

import java.util.ArrayList;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Subscribe;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LDRoidApplication extends Application {
	private static final String TAG = "LDRoidApplication";
    private static final String SUBSCRIBE_LIST_FILE = "subs";
    private static final String SUBSCRIBELOCAL_LIST_FILE = "subs_local";
    
	private static LDRClient client;
	private static SubscribeLocalList subs;
	
	@Override
	public void onLowMemory() {
		//Log.d(TAG,"onLowMemory");
		saveSubscribeLocalListToFile();
		super.onLowMemory();
	}
	
	@Override
	public void onTerminate() {
		//Log.d(TAG,"onTerminate");
		super.onTerminate();
	}
	
	public LDRClient getClient() {
		if (client == null) {
		    LDRClientAccount account = getAccount();
		    if (account.isEmpty()) {
		    	return null;
			}
			client = new LDRClient(account);
		}
		return client;
	}
	
	public void clearClient() {
		client = null;
	}
	
	public LDRClientAccount getAccount() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		LDRClientAccount account = new LDRClientAccount(
				pref.getString("login_id", null),
				pref.getString("password", null)
			);
		return account;
	}
	
	public SubscribeLocalList getSubscribeLocalList() {
		Log.d(TAG, "getSubscribeLocalList" + (subs != null));
		if (subs == null) {
			subs = loadSubscribeLocalList();
		}
		return subs;
	}
	
	public void setSubscribeLocalList(SubscribeLocalList newSubs) {
		subs = newSubs;
		saveSubscribeLocalListToFile();
	}
	
	// SubscribeLocalListを読み込む(List<Subscribe>をマージした状態で)
    private SubscribeLocalList loadSubscribeLocalList() {
		SubscribeLocalList  sll = null;
		try {
			ObjToFile of = new ObjToFile(this, SUBSCRIBELOCAL_LIST_FILE);
			sll = new SubscribeLocalList(
					loadSubscribeListFromFile(),
					(SubscribeLocalList)of.get("0"));
		}
		catch (Exception e) {
		}
		if (sll == null) {
			return new SubscribeLocalList();
		}
		return sll;
    }
    
	// List<Subscribe> をファイルに書き出す
	public void saveSubscribeListToFile(List<Subscribe> subs) {
		Log.d(TAG, "saveSubsToFile");
		try {
			ObjToFile of = new ObjToFile(this, SUBSCRIBE_LIST_FILE);
			of.put("0", subs);
		}
		catch (Exception e) {
			Toast.makeText(getApplicationContext(), 
					"saveSubscribeListToFile ERROR: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}
	
	// List<Subscribe> をファイルから読み込む
	@SuppressWarnings("unchecked")
	private List<Subscribe> loadSubscribeListFromFile() {
		List<Subscribe> result = null;
		try {
			ObjToFile of = new ObjToFile(this, SUBSCRIBE_LIST_FILE);
			result = (ArrayList<Subscribe>)of.get("0");
		}
		catch (Exception e) {
			
		}
		return result;
	}
	
	// SubscribeLocalList をファイルから読み込む
	public void saveSubscribeLocalListToFile() {
		Log.d(TAG, "saveSubscribeLocalListToFile");
		ObjToFile of = new ObjToFile(this, SUBSCRIBELOCAL_LIST_FILE);
		of.put("0", subs);
	}
}
