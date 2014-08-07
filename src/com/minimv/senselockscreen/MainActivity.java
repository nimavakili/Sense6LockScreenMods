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
					public void onSharedPreferenceChanged(SharedPreferences sprefs, String key) {
						updatePreferences(sprefs);
					}
			};
			prefs.registerOnSharedPreferenceChangeListener(spChanged);
			updatePreferences(prefs);
		}
		
		private void updatePreferences(SharedPreferences sprefs) {
	        SharedPreferences.Editor editor = sprefs.edit();
			if (sprefs.getBoolean("panelAlignBottom", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("hideCarrier");
				preference.setChecked(true);
				editor.putBoolean("hideCarrier", true);
				preference = (CheckBoxPreference) findPreference("largeWidget");
				preference.setEnabled(true);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("largeWidget");
				preference.setEnabled(false);
				preference.setChecked(false);
				editor.putBoolean("largeWidget", false);
			}
			if (sprefs.getBoolean("hidePanel", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHidePanel");
				preference.setChecked(false);
				preference.setEnabled(false);
				editor.putBoolean("nukeHidePanel", false);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHidePanel");
				preference.setEnabled(true);
			}
			if (sprefs.getBoolean("nukeHidePanel", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHorizontalArrows");
				preference.setChecked(true);
				preference.setEnabled(false);
				editor.putBoolean("nukeHorizontalArrows", true);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHorizontalArrows");
				preference.setEnabled(true);
			}
			if (!sprefs.getBoolean("disablePatternScroll", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("improvePattern");
				preference.setChecked(false);
				editor.putBoolean("improvePattern", false);
			}
			editor.commit();
		}
	}
}