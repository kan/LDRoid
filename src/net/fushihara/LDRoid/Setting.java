package net.fushihara.LDRoid;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Setting extends PreferenceActivity implements OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        addPreferencesFromResource(R.xml.setting_subs);
        addPreferencesFromResource(R.xml.setting_feedview);

        initListPreference("feedview_order");
    }
    
    private void initListPreference(String key) {
        Preference p = findPreference(key);
        if (p != null) {
        	ListPreference lp = (ListPreference)p;
        	lp.setOnPreferenceChangeListener(this);
        	updateListPreferenceSummary(lp);
        }
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		ListPreference lp = (ListPreference)preference;
		lp.setValue((String)newValue);
		updateListPreferenceSummary(preference);
		return false;
	}
	
	// ListPreferenceÇÃê›íËì‡óeÇsummaryÇ…ê›íËÇ∑ÇÈ
	private void updateListPreferenceSummary(Preference preference) {
		ListPreference p = (ListPreference)preference;
		p.setSummary(p.getEntry());
	}
    
}
