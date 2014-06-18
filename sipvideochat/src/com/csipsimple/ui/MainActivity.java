package com.csipsimple.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Intent startActivity = new Intent();
    	startActivity.setClass(this,Sipdroid.class);
	    startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(startActivity); 
    	finish();
	}
}
