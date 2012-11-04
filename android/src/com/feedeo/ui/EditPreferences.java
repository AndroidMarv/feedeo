/* Copyright (c) 2008-2011 -- CommonsWare, LLC

	 Licensed under the Apache License, Version 2.0 (the "License");
	 you may not use this file except in compliance with the License.
	 You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

	 Unless required by applicable law or agreed to in writing, software
	 distributed under the License is distributed on an "AS IS" BASIS,
	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 See the License for the specific language governing permissions and
	 limitations under the License.
*/
	 
package com.feedeo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.ListPreference;


public class EditPreferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final ListPreference lim = (ListPreference) findPreference("limit");
        lim.setOnPreferenceChangeListener( 
          new OnPreferenceChangeListener () {
              @Override
              public boolean onPreferenceChange(Preference preference, Object newValue) {
                  mySetSummary(lim, newValue.toString()); 
                  return true;
              }
          } );
        mySetSummary(lim, lim.getValue());       
 
        final ListPreference numd = (ListPreference) findPreference("numdays");
        numd.setOnPreferenceChangeListener( 
          new OnPreferenceChangeListener () {
              @Override
              public boolean onPreferenceChange(Preference preference, Object newValue) {
                  mySetSummary(numd, newValue.toString()); 
                  return true;
              }
          } );
        mySetSummary(numd, numd.getValue());       
    }

    private void mySetSummary(ListPreference pref, String newValue) {
        int idx = pref.findIndexOfValue(newValue);
        String val = (pref.getEntries()[idx]).toString();
        pref.setSummary (val); 
        
    }

}

