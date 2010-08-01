package net.fushihara.LDRoid;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LDRoidApplication extends Application {
	private static LDRClient client;
	
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
}
