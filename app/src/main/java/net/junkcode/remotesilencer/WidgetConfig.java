package net.junkcode.remotesilencer;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class WidgetConfig extends Activity {

	Button configOkButton;
	LinearLayout checkTable;
	EditText et;
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	//private SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.widget_config);
		checkTable = (LinearLayout) findViewById(R.id.checkBoxTable);
		et= (EditText) findViewById(R.id.editTextName);
		DeviceManager deviceManager = new DeviceManager(this);
		//deviceManager.open();
		DeviceManager.RemoteDevice[] remoteDevices = deviceManager.getAllDevicesN();
		deviceManager.close();
        for (DeviceManager.RemoteDevice remoteDevice : remoteDevices) {
            CheckBox cb = new CheckBox(this);
            cb.setText(remoteDevice.name);
            cb.setTag(remoteDevice.id);
            checkTable.addView(cb);
        }
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	    	finish();
	    }
		
		configOkButton= (Button)findViewById(R.id.buttonOK);
		configOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Set<String> set = new HashSet<String>();
				for(int i =0;i< checkTable.getChildCount();i++){
					CheckBox child =(CheckBox) checkTable.getChildAt(i);
					if(child.isChecked())set.add(child.getTag().toString());
				}
				SharedPreferences.Editor sharedPrefEditor = PreferenceManager.getDefaultSharedPreferences(WidgetConfig.this).edit();
				sharedPrefEditor.putString("w_" + mAppWidgetId, et.getText().toString());
				sharedPrefEditor.putBoolean("w_s_" + mAppWidgetId, true);
                sharedPrefEditor.apply();
				
				DeviceManager dm = new DeviceManager(WidgetConfig.this);
				//deviceManager.open();
				dm.addWidgetConnection(mAppWidgetId, set);
				dm.close();
				
				new SilencerWidget().onUpdate(
						WidgetConfig.this,
				        AppWidgetManager.getInstance(WidgetConfig.this),
				        new int[] { mAppWidgetId }
				);
				
				Intent resultValue = new Intent();
				resultValue.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.widget_config, menu);
		return true;
	}
	
}
