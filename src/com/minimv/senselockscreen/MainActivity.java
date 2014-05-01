package com.minimv.senselockscreen;

import com.minimv.senselockscreen.R;
//import android.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
//import android.support.v7.app.ActionBar;
//import android.app.Fragment;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
//import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.os.Build;

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

		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//SharedPreferences prefs = getSharedPreferences("numberPicker.preferences", MODE_WORLD_READABLE);
		//SharedPreferences.Editor editor = prefs.edit();		
	}

	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//return true;
	//}

	//@Override
	//public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		//int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		//return super.onOptionsItemSelected(item);
	//}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager prefManager = getPreferenceManager();
			prefManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
	        addPreferencesFromResource(R.xml.prefs);
			//Log.v("MODE", "Mode: " + getPreferenceManager().getSharedPreferencesMode());
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
	        //SharedPreferences.Editor editor = prefs.edit();
	        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
	        	SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(
						SharedPreferences sprefs, String key) {
						//Log.v("Pref", key);
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
