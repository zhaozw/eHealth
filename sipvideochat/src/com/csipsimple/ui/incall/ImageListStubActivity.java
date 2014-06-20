package com.csipsimple.ui.incall;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import co.il.myTechnologies.bluetooth.ElfitechTablet.openfile.CallbackBundle;
import co.il.myTechnologies.bluetooth.ElfitechTablet.openfile.OpenFileDialog;

import com.csipsimple.R;

public class ImageListStubActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_list_stub);

		ListView lv = (ListView) findViewById(R.id.imageList);

		lv.setAdapter(new MyAdapter(this, new int[5]));

		Button openFile = (Button) findViewById(R.id.openFile);
		openFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//show select file dialog
				showDialog(1);
			}
		});

		Button scheduleCalendar = (Button) findViewById(R.id.scheduleCalendar);
		scheduleCalendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openCalendar();
			}
		});

	}

	/**
	 * show select file dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Map<String, Integer> images = new HashMap<String, Integer>();
		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
		images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
		images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
		images.put("csv", R.drawable.filedialog_file);
		images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
		Dialog dialog = OpenFileDialog.createDialog(id, this,
				"Select csv file", new CallbackBundle() {
					@Override
					public void callback(Bundle bundle) {
						String filepath = bundle.getString("path");
					}
				}, null, images);
		return dialog;
	}

	/**
	 * open calendar
	 */
	private void openCalendar() {
		try {

			// Intent i = new Intent();
			//
			// //Froyo or greater (mind you I just tested this on CM7 and the
			// less than froyo one worked so it depends on the phone...)
			// ComponentName cn = new
			// ComponentName("com.google.android.calendar",
			// "com.android.calendar.LaunchActivity");
			//
			// //less than Froyo
			// // ComponentName cn = new ComponentName("com.android.calendar",
			// "com.android.calendar.LaunchActivity");
			//
			// i.setComponent(cn);
			// startActivity(i);

			Intent i = new Intent(Intent.ACTION_VIEW);
			// Android 2.2+
			i.setData(Uri.parse("content://com.android.calendar/time"));
			// Before Android 2.2+
			// i.setData(Uri.parse("content://calendar/time"));

			startActivity(i);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		if (findViewById(R.id.doc_stub_fullscreen).getVisibility() == View.VISIBLE) {
			findViewById(R.id.doc_stub_fullscreen).setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		int[] resIds;

		public MyAdapter(Context context, int[] resIds) {
			this.mInflater = LayoutInflater.from(context);
			this.resIds = resIds;
		}

		@Override
		public int getCount() {

			return resIds.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			// if (convertView == null) {
			convertView = mInflater.inflate(R.layout.image, null);

			// } else {
			// }

			// ImageView imageView = (ImageView) convertView;

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// display the fullscreen image
					findViewById(R.id.doc_stub_fullscreen).setVisibility(
							View.VISIBLE);
				}
			});

			return convertView;
		}

	}
}
