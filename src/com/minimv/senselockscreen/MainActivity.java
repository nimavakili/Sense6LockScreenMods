package com.minimv.senselockscreen;

import com.minimv.senselockscreen.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

	public static Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment())
					.commit();
		}

	}

	public static class SettingsFragment extends PreferenceFragment {

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager prefManager = getPreferenceManager();
			prefManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
	        addPreferencesFromResource(R.xml.prefs);
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
	        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
	        	SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(
						SharedPreferences sprefs, String key) {
				        SharedPreferences.Editor editor = sprefs.edit();
						if (key.equals("panelAlignBottom")) {
							CheckBoxPreference preference = (CheckBoxPreference) findPreference("removeCarrier");
							preference.setChecked(true);
							editor.putBoolean("removeCarrier", true);
						}
						else if (key.equals("nukeHidePanel")) {
							CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHorizontalArrows");
							preference.setChecked(true);
							editor.putBoolean("nukeHorizontalArrows", true);
						}
						editor.commit();
					}
			};
			prefs.registerOnSharedPreferenceChangeListener(spChanged);
		}
	}
}