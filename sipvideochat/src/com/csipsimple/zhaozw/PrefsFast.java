/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.zhaozw;

import android.content.Context;

import com.csipsimple.api.SipConfigManager;
import com.csipsimple.utils.PreferencesWrapper;

public class PrefsFast{
	
//	private CheckBox globIntegrate;
//	private RadioButton globProfileAlways;
//	private RadioButton globProfileWifi;
//	private RadioButton globProfileNever;
//	private CheckBox globGsm;
//	Context ctxt;
	
	enum Profile {
		UNKOWN,
		ALWAYS,
		WIFI,
		NEVER
	}
//	public PrefsFast(Context ctxt) {
//		this.ctxt = ctxt;
//	}
//	protected void onCreate() {
//		
//		updateFromPrefs();
//		
//	}
	

//	@Override
//	public void onDestroy(){
//		super.onDestroy();
//		applyPrefs();
//	}
	
//	private void updateFromPrefs() {
//		globIntegrate.setChecked(SipConfigManager.getPreferenceBooleanValue(ctxt, SipConfigManager.INTEGRATE_WITH_DIALER));
//		
//		globGsm.setChecked( useGsm );
//		
//		setProfile(mode);
//	}
//
//	private void setProfile(Profile mode) {
//		globProfileAlways.setChecked(mode == Profile.ALWAYS);
//		globProfileWifi.setChecked(mode == Profile.WIFI);
//		globProfileNever.setChecked(mode == Profile.NEVER);
//	}

//	@Override
//	public void onClick(View v) {
//		int id = v.getId();
//		if(id == R.id.glob_profile_always || id == R.id.row_glob_profile_always) {
//			setProfile(Profile.ALWAYS);
//		
//		}else if(id == R.id.glob_profile_wifi || id == R.id.row_glob_profile_wifi) {
//			setProfile(Profile.WIFI);
//		
//		}else if(id == R.id.glob_profile_never || id == R.id.row_glob_profile_never) {
//			setProfile(Profile.NEVER);
//			return;
//		}else if( id == R.id.row_glob_integrate ) {
//			globIntegrate.toggle();
//		}else if( id == R.id.row_glob_tg ) {
//			globGsm.toggle();
//		}else if (id == R.id.save_bt) {
//			if(!SipConfigManager.getPreferenceBooleanValue(this, PreferencesWrapper.HAS_ALREADY_SETUP, false) ) {
//			    SipConfigManager.setPreferenceBooleanValue(this, PreferencesWrapper.HAS_ALREADY_SETUP, true);
//			}
//			finish();
//		}
//	}
	
}
