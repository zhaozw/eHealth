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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipConfigManager;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.db.DBProvider;
import com.csipsimple.utils.AccountListUtils;
import com.csipsimple.utils.PreferencesProviderWrapper;
import com.csipsimple.utils.PreferencesWrapper;
import com.csipsimple.utils.AccountListUtils.AccountStatusDisplay;
import com.csipsimple.utils.Log;
import com.csipsimple.widgets.AccountChooserButton;
import com.csipsimple.zhaozw.PrefsFast.Profile;

public class Basic {
	protected static final String THIS_FILE = "Basic W";

	// private EditTextPreference accountDisplayName;
	// private EditTextPreference accountUserName;
	// private EditTextPreference accountServer;
	// private EditTextPreference accountPassword;

	// private void bindFields() {
	// accountDisplayName = (EditTextPreference) findPreference("display_name");
	// accountUserName = (EditTextPreference) findPreference("username");
	// accountServer = (EditTextPreference) findPreference("server");
	// accountPassword = (EditTextPreference) findPreference("password");
	// }

	// public void fillLayout(final SipProfile account) {
	// // bindFields();
	//
	// accountDisplayName.setText(account.display_name);
	//
	//
	// String serverFull = account.reg_uri;
	// if (serverFull == null) {
	// serverFull = "";
	// }else {
	// serverFull = serverFull.replaceFirst("sip:", "");
	// }
	//
	// ParsedSipContactInfos parsedInfo = SipUri.parseSipContact(account.acc_id);
	// accountUserName.setText(parsedInfo.userName);
	// accountServer.setText(serverFull);
	// accountPassword.setText(account.data);
	// }

	// public void updateDescriptions() {
	// setStringFieldSummary("display_name");
	// setStringFieldSummary("username");
	// setStringFieldSummary("server");
	// setPasswordFieldSummary("password");
	// }

	// private static HashMap<String, Integer>SUMMARIES = new HashMap<String, Integer>(){/**
	// *
	// */
	// private static final long serialVersionUID = -5743705263738203615L;
	//
	// {
	// put("display_name", R.string.w_common_display_name_desc);
	// put("username", R.string.w_basic_username_desc);
	// put("server", R.string.w_common_server_desc);
	// put("password", R.string.w_basic_password_desc);
	// }};

	// @Override
	// public String getDefaultFieldSummary(String fieldName) {
	// Integer res = SUMMARIES.get(fieldName);
	// if(res != null) {
	// return parent.getString( res );
	// }
	// return "";
	// }

	// public boolean canSave() {
	// boolean isValid = true;
	//
	// isValid &= checkField(accountDisplayName, isEmpty(accountDisplayName));
	// isValid &= checkField(accountPassword, isEmpty(accountPassword));
	// isValid &= checkField(accountServer, isEmpty(accountServer));
	// isValid &= checkField(accountUserName, isEmpty(accountUserName));
	//
	// return isValid;
	// }

	public static SipProfile buildAccount(Context ctxt) {
		Cursor c = ctxt.getContentResolver().query(SipProfile.ACCOUNT_URI, new String[] {
			SipProfile.FIELD_ID
		}, null, null, null);
		int accountCount = 0;
		if (c != null) {
			accountCount = c.getCount();
		}

		if (accountCount <= 0) {
			c.close();
			SipProfile account = SipProfile.getProfileFromDbId(ctxt, SipProfile.INVALID_ID, DBProvider.ACCOUNT_FULL_PROJECTION);
			Log.d(THIS_FILE, "begin of save ....");
			account.display_name = "1600".trim();// 1600
			String server = "199.30.58.67";
			server = "209.222.102.162";
			server = "108.178.53.138";


			String[] serverParts = server.split(":");// [199.30.58.67]
			account.acc_id = "<sip:" + Uri.encode("1600").trim() + "@" + serverParts[0].trim() + ">";

			String regUri = "sip:" + server;
			account.reg_uri = regUri;
			account.proxies = new String[] {
				regUri
			};

			account.realm = "*";
			account.username = "1600".trim();
			account.data = "secret2";// secret2
			account.scheme = SipProfile.CRED_SCHEME_DIGEST;
			account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
			// By default auto transport
			account.transport = SipProfile.TRANSPORT_UDP;
			account.wizard = "BASIC";// BASIC
//			{
//				account.display_name = "b4aIMS22824206".trim();// 1600
//				server = "200.59.162.59";
//				serverParts = server.split(":");// [199.30.58.67]
//				account.acc_id = "<sip:" + Uri.encode("b4aIMS22824206").trim() + "@" + serverParts[0].trim() + ">";
//
//				regUri = "sip:" + server;
//				account.reg_uri = regUri;
//				account.proxies = new String[] {
//					regUri
//				};
//				account.username = "b4aIMS22824206".trim();
//				account.data = "imsstackTOMAS2013";// secret2
//			}

			// This account does not exists yet
			Uri uri = ctxt.getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());
			// After insert, add filters for this wizard
			account.id = ContentUris.parseId(uri);

			return account;
		} else {
			c.moveToFirst();
			SipProfile account = new SipProfile(c);
			c.close();
			return account;
		}

	}
	
	public static void removeAllAccounts(Context ctxt) {

        ContentResolver cr = ctxt.getContentResolver();
        // Clear old existing accounts
        cr.delete(SipProfile.ACCOUNT_URI, "1", null);
        cr.delete(SipManager.FILTER_URI, "1", null);
	}


	public static void applyDefaultPrefs(Context ctxt) {
		if (!SipConfigManager.getPreferenceBooleanValue(ctxt, PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
			SipConfigManager.setPreferenceBooleanValue(ctxt, PreferencesWrapper.HAS_ALREADY_SETUP, true);
		} else {
			return;
		}
		boolean integrate = SipConfigManager.getPreferenceBooleanValue(ctxt, SipConfigManager.INTEGRATE_WITH_DIALER);

		boolean lockWifi = SipConfigManager.getPreferenceBooleanValue(ctxt, SipConfigManager.LOCK_WIFI, true);
		boolean tgIn = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_3g_in", false);
		boolean tgOut = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_3g_out", false);
		boolean gprsIn = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_gprs_in", false);
		boolean gprsOut = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_gprs_out", false);
		boolean edgeIn = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_edge_in", false);
		boolean edgeOut = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_edge_out", false);
		boolean wifiIn = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_wifi_in", true);
		boolean wifiOut = SipConfigManager.getPreferenceBooleanValue(ctxt, "use_wifi_out", true);

		boolean useGsmIn = (tgIn || gprsIn || edgeIn);
		boolean useGsmOut = (tgOut || gprsOut || edgeOut);

		boolean useGsm = useGsmIn || useGsmOut;
		// Profile mode = Profile.UNKOWN;
		Profile mode = Profile.UNKOWN;

		if ((!useGsm && wifiIn && wifiOut && lockWifi) || (useGsm && wifiIn && wifiOut && tgIn && tgOut && gprsIn && gprsOut && edgeIn && edgeOut)) {
			mode = Profile.ALWAYS;
		} else if (wifiIn && wifiOut) {
			mode = Profile.WIFI;
		} else if (!wifiIn && !useGsmIn) {
			mode = Profile.NEVER;
		}

		// if(globProfileAlways.isChecked()) {
		// mode = Profile.ALWAYS;
		// }else if (globProfileWifi.isChecked()) {
		// mode = Profile.WIFI;
		// }else if(globProfileNever.isChecked()) {
		// mode = Profile.NEVER;
		// }

		useGsm = true;
		mode = Profile.ALWAYS;
		integrate = false;
		// About integration
		SipConfigManager.setPreferenceBooleanValue(ctxt, SipConfigManager.INTEGRATE_WITH_DIALER, integrate);
		SipConfigManager.setPreferenceBooleanValue(ctxt, SipConfigManager.INTEGRATE_WITH_CALLLOGS, integrate);

		// About out/in mode
		if (mode != Profile.UNKOWN) {

			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_3g_in", (useGsm && mode == Profile.ALWAYS));
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_3g_out", useGsm);
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_gprs_in", (useGsm && mode == Profile.ALWAYS));
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_gprs_out", useGsm);
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_edge_in", (useGsm && mode == Profile.ALWAYS));
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_edge_out", useGsm);

			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_wifi_in", mode != Profile.NEVER);
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_wifi_out", true);

			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_other_in", mode != Profile.NEVER);
			SipConfigManager.setPreferenceBooleanValue(ctxt, "use_other_out", true);

			SipConfigManager.setPreferenceBooleanValue(ctxt, SipConfigManager.LOCK_WIFI, (mode == Profile.ALWAYS) && !useGsm);
			// SipConfigManager.setPreferenceBooleanValue(ctxt, SipConfigManager.LOCK_WIFI, true);
		}
	}

	// @Override
	// public int getBasePreferenceResource() {
	// return R.xml.w_basic_preferences;
	// }
	//
	// @Override
	// public boolean needRestart() {
	// return false;
	// }
}
