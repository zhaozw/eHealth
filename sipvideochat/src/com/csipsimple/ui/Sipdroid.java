/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.csipsimple.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javazoom.jl.converter.Converter;
import android.app.Activity;
import android.app.PendingIntent.CanceledException;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import com.csipsimple.R;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.service.OutgoingCall;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.AccountListUtils;
import com.csipsimple.utils.AccountListUtils.AccountStatusDisplay;
import com.csipsimple.utils.CallHandlerPlugin;
import com.csipsimple.utils.CallHandlerPlugin.OnLoadListener;
import com.csipsimple.utils.CustomDistribution;
import com.csipsimple.utils.Log;
import com.csipsimple.utils.PreferencesProviderWrapper;
import com.csipsimple.utils.PreferencesWrapper;
import com.csipsimple.zhaozw.Basic;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class Sipdroid extends Activity implements OnDismissListener {


	private static final String CSIPSIMPLE = "/CSipSimple/";//CustomDistribution.getSDCardFolder()

	private static final String SOUND_FOLDER = Environment.getExternalStorageDirectory() + CSIPSIMPLE/*+"sounds/"*/;

	AutoCompleteTextView /* sip_uri_box, */sip_uri_box2;

	Button dialButton;

	Button hangupButton;

	Button speakerphoneButton;

	Button playMp3Button;

	Button recordButton;

	Chronometer mElapsedTime = null;
	private Spinner supplier;

	public static Sipdroid instance;

	public static final int ACCOUNTS_MENU = Menu.FIRST + 1;

	public static final int PARAMS_MENU = Menu.FIRST + 2;

	public static final int CLOSE_MENU = Menu.FIRST + 3;

	public static final int HELP_MENU = Menu.FIRST + 4;

	public static final int DISTRIB_ACCOUNT_MENU = Menu.FIRST + 5;

	private static final String THIS_FILE = "SIP_HOME";
	
	private PreferencesProviderWrapper prefProviderWrapper;

	private boolean hasTriedOnceActivateAcc = false;

	public final static boolean USE_LIGHT_THEME = false;

	private boolean inited = false;
	private boolean recorded = false;
	private ProgressDialog pd;

	// @Override
	public void onStart() {
		super.onStart();
		
		ContentResolver content = getContentResolver();
		Cursor cursor = content.query(Calls.CONTENT_URI, PROJECTION, Calls.NUMBER + " like ?", new String[] {
			"%@%"
		}, Calls.DEFAULT_SORT_ORDER);
		CallsAdapter adapter = new CallsAdapter(this, cursor);
		// sip_uri_box.setAdapter(adapter);
		sip_uri_box2.setAdapter(adapter);
	}

	public static class CallsAdapter extends CursorAdapter implements Filterable {
		public CallsAdapter(Context context, Cursor c) {
			super(context, c);
			mContent = context.getContentResolver();
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
			String phoneNumber = cursor.getString(1);
			view.setText(phoneNumber);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String phoneNumber = cursor.getString(1);
			((TextView) view).setText(phoneNumber);
		}

		@Override
		public String convertToString(Cursor cursor) {
			String phoneNumber = cursor.getString(1);
			if (phoneNumber.contains(" <"))
				phoneNumber = phoneNumber.substring(0, phoneNumber.indexOf(" <"));
			return phoneNumber;
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			if (getFilterQueryProvider() != null) {
				return new CallsCursor(getFilterQueryProvider().runQuery(constraint));
			}

			StringBuilder buffer;
			String[] args;
			buffer = new StringBuilder();
			buffer.append(Calls.NUMBER);
			buffer.append(" LIKE ? OR ");
			buffer.append(Calls.CACHED_NAME);
			buffer.append(" LIKE ?");
			String arg = "%" + (constraint != null && constraint.length() > 0 ? constraint.toString() : "@") + "%";
			args = new String[] {
					arg, arg
			};

			return new CallsCursor(mContent.query(Calls.CONTENT_URI, PROJECTION, buffer.toString(), args, Calls.NUMBER + " asc"));
		}

		private ContentResolver mContent;
	}

	public static class CallsCursor extends CursorWrapper {
		List<String> list;
		
		public int getCount() {
			return list.size();
		}
		
		public String getString(int i) {
			return list.get(getPosition());
		}
		
		public CallsCursor(Cursor cursor) {
			super(cursor);
			list = new ArrayList<String>();
			for (int i = 0; i < cursor.getCount(); i++) {
				moveToPosition(i);
 		        String phoneNumber = super.getString(1);
		        String cachedName = super.getString(2);
		        if (cachedName != null && cachedName.trim().length() > 0)
		        	phoneNumber += " <" + cachedName + ">";
		        if (list.contains(phoneNumber)) continue;
				list.add(phoneNumber);
			}
			moveToFirst();
		}
		
	}
	
	private static final String[] PROJECTION = new String[] {
			Calls._ID, Calls.NUMBER, Calls.CACHED_NAME
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		instance = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipdroid);
		prefProviderWrapper = new PreferencesProviderWrapper(this);
		
		sip_uri_box2 = (AutoCompleteTextView) findViewById(R.id.txt_callee2);
		
		sip_uri_box2.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					call_menu(sip_uri_box2);
					return true;
				}
				return false;
			}
		});
		sip_uri_box2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				call_menu(sip_uri_box2);
			}
		});

		dialButton = (Button) findViewById(R.id.dialButton);
		dialButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				call_menu(sip_uri_box2);
			}
		});
		hangupButton = (Button) findViewById(R.id.hangupButton);
		hangupButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (service != null) {
					try {
						callsInfo = service.getCalls();
						for (SipCallSession callInfo : callsInfo) {
							service.hangup(callInfo.getCallId(), 0);
							if (recorded) {
								service.stopRecording(callInfo.getCallId());
								
							}
						}
						recordButton.setText("Record");
						recorded = false;
						acc = null;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
		speakerphoneButton = (Button) findViewById(R.id.speakerphoneButton);
		speakerphoneButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Receiver.engine(Sipdroid.this).speaker(AudioManager.MODE_IN_CALL);
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				// audioManager.setSpeakerphoneOn(!audioManager.isSpeakerphoneOn());
				if (service != null) {
					try {
						service.setSpeakerphoneOn((!audioManager.isSpeakerphoneOn()) ? true : false);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		playMp3Button = (Button) findViewById(R.id.playButton);
		playMp3Button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (service != null) {
					try {
						callsInfo = service.getCalls();
						for (SipCallSession callInfo : callsInfo) {
							service.playWaveFile(fileFullName.get(supplier.getSelectedItemPosition()), callInfo.getCallId(), 3);
	//						service.playWaveFile("android.resource://com.csipsimple/raw/test", callInfo.getCallId(), 3);
						}
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				
////				final int inCallStream = Compatibility.getInCallStream();
//				final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//				final boolean isSpeakerphoneOn = audioManager.isSpeakerphoneOn();
//				if (!isSpeakerphoneOn) {
//					if (service != null) {
//						try {
//							service.setSpeakerphoneOn(true);
//						} catch (RemoteException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
////					audioManager.setSpeakerphoneOn(isSpeakerphoneOn);
//				}
//				
//				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
////				mAudioManager.setStreamSolo(inCallStream, false);
////		        int mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); //获取当前音乐音量
//		        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//				MediaPlayer mPlayer1 = MediaPlayer.create(Sipdroid.this, R.raw.test);
//				mPlayer1.setOnCompletionListener(new OnCompletionListener(){
//
//					@Override
//					public void onCompletion(MediaPlayer mp) {
//						// TODO Auto-generated method stub
////						((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setStreamSolo(inCallStream, true);
//						if (!isSpeakerphoneOn) {
//							if (service != null) {
//								try {
//									service.setSpeakerphoneOn(false);
//								} catch (RemoteException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							}
////							audioManager.setSpeakerphoneOn(false);
//						}
//					}
//				});
//				mPlayer1.start();
			}
		});
		
		recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (service != null) {
					try {
						callsInfo = service.getCalls();
						if (callsInfo.length==0) {
							return;
						}
						for (SipCallSession callInfo : callsInfo) {
							if (recorded) {
								service.stopRecording(callInfo.getCallId());
								recordButton.setText("Record");
							} else {
								service.startRecording(callInfo.getCallId(),SipManager.BITMASK_IN|SipManager.BITMASK_OUT);
								recordButton.setText("Stop record");
							}
		//						service
		//				        .startRecording(callInfo.getCallId(), SipManager.BITMASK_IN | SipManager.BITMASK_OUT);
						}
						recorded = !recorded;
					} catch (RemoteException e1) {
					}
				}
			}
		});
		supplier = (Spinner) findViewById(R.id.supplier);

		status = (TextView) findViewById(R.id.status);

		mElapsedTime = (Chronometer) findViewById(R.id.elapsedTime);
		// Text colors
		mTextColorConnected = getResources().getColor(R.color.incall_textConnected);
		mTextColorEnded = getResources().getColor(R.color.incall_textEnded);
		
		bindService(new Intent(this, SipService.class), connection1, Context.BIND_AUTO_CREATE);

		// Listen to media & sip events to update the UI
		registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_CALL_CHANGED));
		registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_MEDIA_CHANGED));
		registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_ZRTP_SHOW_SAS));

		File dir= new File(SOUND_FOLDER+"sounds/");
		if (dir.exists()) {
			displayFiles();
		} else {
//	        pd = ProgressDialog.show(this, "Copying an converting audio files", "Please wait");  
//	        pd.setMax(100);

			new DownloadTask().execute((Void)null);
		}
		
//		put(SipConfigManager.AUTO_RECORD_CALLS, false);
//        pjService
//        .startRecording(callId, SipManager.BITMASK_IN | SipManager.BITMASK_OUT);
//		PreferencesWrapper.getRecordsFolder(this);
//		SipConfigManager.AUTO_RECORD_CALLS

	}
	Converter converter = new Converter();

//	public static void copyFile(InputStream in, OutputStream out) throws IOException {
//		byte[] buffer = new byte[1024];
//		int read;
//		while ((read = in.read(buffer)) != -1) {
//			out.write(buffer, 0, read);
//		}
//	}

	List<String> fileNames = new ArrayList<String>();
	List<String> fileFullName = new ArrayList<String>();
	private void displayFiles() {
		File f = new File(SOUND_FOLDER+"sounds/");
		initFileList(f);
		if (fileNames.isEmpty()) {
			return;
		}

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//				this, R.layout.spinner_item, fileNames);
//		adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		supplier.setAdapter(adapter);
		supplier.setSelection(0);
		if (pd!=null) {
			try {
				pd.dismiss();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private void initFileList(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				initFileList(files[i]);
			}
		} else {
			if (f.getName().endsWith(".wav")) {
				fileNames.add(f.getName());
				fileFullName.add(f.getAbsolutePath());
			}
		}
	}
	private ISipService service;

	void call_menu(AutoCompleteTextView view) {
		// TODO call

		if (service == null) {
			return;
		}
		String toCall = "";
		accountToUse = SipProfile.INVALID_ID;
		acc = getDefaultAccount();
		// SipProfile acc = accountChooserButton.getSelectedAccount();
		if (acc != null) {
			accountToUse = acc.id;
		} else {
			return;
		}
		toCall = PhoneNumberUtils.stripSeparators(view.getText().toString());

		if (TextUtils.isEmpty(toCall)) {
			return;
		}

		// Well we have now the fields, clear theses fields
		// view.getText().clear();

		// -- MAKE THE CALL --//
		if (accountToUse >= 0) {
			// It is a SIP account, try to call service for that
			try {
				service.makeCall(toCall, accountToUse.intValue());
			} catch (RemoteException e) {
				Log.e(THIS_FILE, "Service can't be called to make the call");
			}
		} else if (accountToUse != SipProfile.INVALID_ID) {

            CallHandlerPlugin ch = new CallHandlerPlugin(this);
            ch.loadFrom(accountToUse, toCall, new OnLoadListener() {
                @Override
                public void onLoad(CallHandlerPlugin ch) {
                    placePluginCall(ch);
                }
            });
		}

	}

	private SipProfile getDefaultAccount() {
		return Basic.buildAccount(this);
	}

	private void placePluginCall(CallHandlerPlugin ch) {
		try {
			String nextExclude = ch.getNextExcludeTelNumber();
			if (nextExclude != null) {
				OutgoingCall.ignoreNext = nextExclude;
			}
			ch.getIntent().send();
		} catch (CanceledException e) {
			Log.e(THIS_FILE, "Pending intent cancelled", e);
		}
	}

	// Service monitoring stuff
	private void startSipService() {
		Thread t = new Thread("StartSip") {
			public void run() {
				Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
				serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(Sipdroid.this, Sipdroid.class));
				startService(serviceIntent);
				postStartSipService();
			};
		};
		t.start();

	}

	private void postStartSipService() {
		// If we have never set fast settings
		if (CustomDistribution.showFirstSettingScreen()) {
			if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false) && !inited) {
				inited = true;
				Basic.applyDefaultPrefs(this);
			}
		} else {
		}
		Basic.buildAccount(this);
		// If we have no account yet, open account panel,
		if (!hasTriedOnceActivateAcc) {
			hasTriedOnceActivateAcc = true;
		}
	}

	@Override
	protected void onPause() {
		Log.d(THIS_FILE, "On Pause SIPHOME");
		super.onPause();
		if (statusObserver != null) {
			getContentResolver().unregisterContentObserver(statusObserver);
		}

	}

	@Override
	protected void onResume() {
		Log.d(THIS_FILE, "On Resume SIPHOME");
		super.onResume();

		prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_BEEN_QUIT, false);

		// Set visible the currently selected account
		// sendFragmentVisibilityChange(mViewPager.getCurrentItem(), true);

		Log.d(THIS_FILE, "WE CAN NOW start SIP service");
		startSipService();

		statusObserver = new AccountStatusContentObserver(mHandler);
		getContentResolver().registerContentObserver(SipProfile.ACCOUNT_STATUS_URI, true, statusObserver);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		onResume();
	}

	private void disconnect(boolean quit) {
		Log.d(THIS_FILE, "True disconnection...");
		Intent intent = new Intent(SipManager.ACTION_OUTGOING_UNREGISTER);
		intent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(this, Sipdroid.class));
		sendBroadcast(intent);
		if (quit) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			unbindService(connection1);
		} catch (Exception e) {
			// Just ignore that
			Log.w(THIS_FILE, "Unable to un bind", e);
		}
		disconnect(false);
		super.onDestroy();
//		System.exit(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
			serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(Sipdroid.this, Sipdroid.class));
			stopService(serviceIntent);
			finish();
		}else if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
			serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(Sipdroid.this, Sipdroid.class));
			stopService(serviceIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}


	Handler mHandler1 = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				status.setText(msg.obj.toString());
			} else {
				status.setText("");
			}

		}
	};

	Handler mHandler2 = new Handler() {
		public void handleMessage(Message msg) {
			// TODO
		}
	};

	private TextView status;

	// Text colors, used with the lower title and "other call" info areas
	private int mTextColorConnected;

	private int mTextColorEnded;

	SipProfile acc = null;

	private Long accountToUse;

	private SipCallSession[] callsInfo = null;

	private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
				if (service != null) {
					try {
						callsInfo = service.getCalls();
					} catch (RemoteException e) {
						Log.e(THIS_FILE, "Not able to retrieve calls");
					}
				}

				handler.sendMessage(handler.obtainMessage(UPDATE_FROM_CALL));
			} else if (action.equals(SipManager.ACTION_SIP_MEDIA_CHANGED)) {
				handler.sendMessage(handler.obtainMessage(UPDATE_FROM_MEDIA));
			} else if (action.equals(SipManager.ACTION_ZRTP_SHOW_SAS)) {
				ZrtpSasInfo sasInfo = new ZrtpSasInfo();
				sasInfo.sas = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				sasInfo.dataPtr = intent.getIntExtra(Intent.EXTRA_UID, 0);
				handler.sendMessage(handler.obtainMessage(SHOW_SAS, sasInfo));
			}
		}
	};

	private class ZrtpSasInfo {
		public String sas;

		public int dataPtr;
	}

	private static final int UPDATE_FROM_CALL = 1;

	private static final int UPDATE_FROM_MEDIA = 2;

	private static final int UPDATE_DRAGGING = 3;

	private static final int SHOW_SAS = 4;

	// Ui handler
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_FROM_CALL:
				updateUIFromCall();
				break;
			case UPDATE_FROM_MEDIA:
				break;
			case UPDATE_DRAGGING:
				break;
			case SHOW_SAS:
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	private synchronized void updateUIFromCall() {
		if (!serviceConnected) {
			return;
		}
		SipCallSession callInfo = getActiveCallInfo();
		updatemElapsedTimer(callInfo);
	}

	private SipCallSession getPrioritaryCall(SipCallSession call1, SipCallSession call2) {
		// We prefer the not null
		if (call1 == null) {
			return call1;
		} else if (call2 == null) {
			return call1;
		}
		// We prefer the one not terminated
		if (call1.isAfterEnded()) {
			return call2;
		} else if (call2.isAfterEnded()) {
			return call1;
		}
		// We prefer the one not held
		if (call1.isLocalHeld()) {
			return call2;
		} else if (call2.isLocalHeld()) {
			return call1;
		}
		// We prefer the most recent
		return (call1.getCallStart() > call2.getCallStart()) ? call2 : call1;
	}

	private SipCallSession getActiveCallInfo() {
		SipCallSession currentCallInfo = null;
		try {
			callsInfo = service.getCalls();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (callsInfo == null) {
			return null;
		}
		for (SipCallSession callInfo : callsInfo) {
			currentCallInfo = getPrioritaryCall(callInfo, currentCallInfo);
		}
		return currentCallInfo;
	}

	private void updatemElapsedTimer(SipCallSession callInfo) {

		if (callInfo == null || acc == null) {
			mElapsedTime.stop();
			if (accountToUse == null) {
				mElapsedTime.setVisibility(View.GONE);
			} else {
				mElapsedTime.setVisibility(View.VISIBLE);
			}

			mElapsedTime.setTextColor(mTextColorEnded);
			return;
		}

		mElapsedTime.setBase(callInfo.getConnectStart());

		int state = callInfo.getCallState();
		switch (state) {
		case SipCallSession.InvState.INCOMING:
		case SipCallSession.InvState.CALLING:
		case SipCallSession.InvState.EARLY:
		case SipCallSession.InvState.CONNECTING:
			mElapsedTime.setVisibility(View.VISIBLE);
			mElapsedTime.start();
			break;
		case SipCallSession.InvState.CONFIRMED:
			Log.v(THIS_FILE, "we start the timer now ");
			mElapsedTime.start();
			mElapsedTime.setVisibility(View.VISIBLE);
			mElapsedTime.setTextColor(mTextColorConnected);

			break;
		case SipCallSession.InvState.NULL:
		case SipCallSession.InvState.DISCONNECTED:
			mElapsedTime.stop();
			mElapsedTime.setVisibility(View.VISIBLE);
			mElapsedTime.setTextColor(mTextColorEnded);
			break;
		default:
			break;
		}

	}

	private boolean serviceConnected = false;

	private ServiceConnection connection1 = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = ISipService.Stub.asInterface(arg1);
			try {
				// Log.d(THIS_FILE,
				// "Service started get real call info "+callInfo.getCallId());
				callsInfo = service.getCalls();
				serviceConnected = true;
				handler.sendMessage(handler.obtainMessage(UPDATE_FROM_CALL));
				handler.sendMessage(handler.obtainMessage(UPDATE_FROM_MEDIA));
			} catch (RemoteException e) {
				Log.e(THIS_FILE, "Can't get back the call", e);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			serviceConnected = false;
			callsInfo = null;
		}
	};

	class AccountStatusContentObserver extends ContentObserver {

		public AccountStatusContentObserver(Handler h) {
			super(h);
		}

		public void onChange(boolean selfChange) {
			Log.d(THIS_FILE, "Accounts status.onChange( " + selfChange + ")");
			final SipProfile account = getDefaultAccount();
			// Update status label and color
			if (account.active) {
				AccountStatusDisplay accountStatusDisplay = AccountListUtils.getAccountDisplay(Sipdroid.this, account.id);
				Message msg = new Message();
				msg.obj = accountStatusDisplay.statusLabel;
				mHandler1.sendMessage(msg);
			} else {
				Message msg = new Message();
				msg.obj = R.string.acct_inactive;
				mHandler1.sendMessage(msg);
			}
		}
	}

	private final Handler mHandler = new Handler();

	private AccountStatusContentObserver statusObserver = null;
	

	private class DownloadTask extends AsyncTask<Void, Integer, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
//			copyProgress.setText("0%");

	        pd = new ProgressDialog(Sipdroid.this);
	        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        pd.setMessage("Copying an converting audio files");
//	        pd.setIndeterminate(true);
	        pd.setCancelable(false);   
	        pd.setMax(100);
	        pd.setProgress(0);
	        pd.show();
		}

		@Override
		protected String doInBackground(Void... voids) {
			copyFile();
			return null;
		}
		private void copyFile() {
			try {
				count=0;
				copied=0;
				File wallpaperDirectory = new File(
						SOUND_FOLDER);
				// if (wallpaperDirectory.exists())
				// return;
				wallpaperDirectory.mkdirs();
				AssetManager assetManager = getAssets();
				String[] files = null;
				try {
					files = assetManager.list("");
//					files = assetManager.list("sounds");
				} catch (IOException e) {
					Log.e("tag", e.getMessage());
				}
				for (String filename : files) {
//					copyFileOrDir("sounds/"+filename,false);
					if (filename.equals("sounds")) {
						copyFileOrDir(filename,false);
					}
					
				}
				for (String filename : files) {
//					copyFileOrDir("sounds/"+filename,true);
					if (filename.equals("sounds")) {
						copyFileOrDir(filename,true);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void copyFileOrDir(String path,boolean copy) {
		    AssetManager assetManager = getAssets();
		    String assets[] = null;
		    try {
//				Converter converter = new Converter();
		        assets = assetManager.list(path);
		        if (assets.length == 0) {
		            copyFile(path,copy);
		        } else {
		            String fullPath = SOUND_FOLDER + path;
		            File dir = new File(fullPath);
		            if (!dir.exists())
		                dir.mkdir();
		            for (int i = 0; i < assets.length; ++i) {
		                copyFileOrDir(path + "/" + assets[i],copy);
		            }
		        }
		    } catch (IOException ex) {
		        Log.e("tag", "I/O Exception", ex);
		    }
		}
		int count=0;
		int copied=0;

		private void copyFile(String filename,boolean copy) {
		    AssetManager assetManager = getAssets();

		    InputStream in = null;
//		    try {
//		        in = assetManager.open(filename);
//				converter.convert(in, SOUND_FOLDER + filename.replace(".mp3", ".wav"), null, null);
//		        in.close();
//		        in = null;
//		    } catch (Exception e) {
//		        Log.e("tag", e.getMessage());
//		    }
		    

			if (!filename.endsWith(".mp3")) {
				return;
			}
			String newName = filename.replace(".mp3", ".wav");
			File f = new File(SOUND_FOLDER + newName);
			if (f.exists() && f.length() > 10) {
				return;
			}
			if (!copy) {
				count++;
				return;
			}
			try {
				in = assetManager.open(filename);
				converter.convert(in, SOUND_FOLDER + newName, null, null);
				in.close();
				in = null;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
			copied++;
//			(copied*100/count)+"%";
//			handler.post(new Runnable() {
//				public void run() {
//					pd.setProgress((copied*100/count));
//				}
//			});

			publishProgress((copied*100/count));

		}

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
//	        Log.v("count",progress[0]);
	        pd.setProgress(progress[0]);
	   }
		@Override
		protected void onPostExecute(String result) {
//			copyProgress.setText("100%");
			pd.setProgress(100);
			displayFiles();
		}

	}
}
