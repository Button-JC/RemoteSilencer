package net.junkcode.remotesilencer;

import static net.junkcode.remotesilencer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static net.junkcode.remotesilencer.CommonUtilities.DISPLAY_REFRESH_UI;
import static net.junkcode.remotesilencer.CommonUtilities.EXTRA_MESSAGE;
import static net.junkcode.remotesilencer.CommonUtilities.MESSAGE_FROM;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.SingleLineTransformationMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


/**
 * Created by Button
 * Contact: Button@junkcode.net
 * Note this code has bases on
 * https://code.google.com/p/gcm/source/browse/gcm-client/GcmClient/src/main/java/com/google/android/gcm/demo/app/DemoActivity.java
 * and its previous versions
 */
public class MainActivity extends SherlockActivity{


    private static final int    PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    AudioManager audioManager;
    private static final String TAG = "RS_main";

    private SharedPreferences  sharedPref;
    private DeviceManager      deviceManager;
    private ServerCommunicator serverCommunicator;
    static AlertDialog pairCodeDialog = null;
    private Context context;
    //private String lists[]={"devices","devices_access"};

    /**
     * Setup app on creation
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout
        setContentView(R.layout.activity_main);


        //get application context
        context = this;

        //get preferences storage
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //deviceManager = new DeviceManager(this);




        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            serverCommunicator = new ServerCommunicator(context);
        } else {
            Log.e(TAG, "No valid Google Play Services APK found.");
            Toast.makeText(context, "This app requires Google Play Services", Toast.LENGTH_LONG).show();
        }

        /** Update message **/
        String updated = "updated_to_1_10";
        if (sharedPref.getBoolean(updated, true)){    // this is new update
            sharedPref.edit().putBoolean(updated, false).apply();
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ScrollView msg = (ScrollView) mInflater.inflate(R.layout.message, null);
            TextView fulltext = (TextView) msg.findViewById(R.id.message_text);
            TextView text = (TextView) msg.findViewById(R.id.version);
            text.setText(context.getString(R.string.version) + " " + getAppVersion());
            int title;
            if (!sharedPref.getString("log", "").isEmpty()){   // new install
                fulltext.setText(R.string.info_msg);
                title = R.string.info_title;
            }else { // just update
                fulltext.setText(R.string.info_update);
                title = R.string.update_title;
            }
            CommonUtilities.doDialog(title, msg, null, context);
        }
    }

    private String getAppVersion() {
        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "not accessible";
            Log.e("(app version", e.getMessage() + ")");
        }
        return versionName;
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                                                      PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Table of paired devices to control
     */
    public void reBuildDeviceTable(){
		TableLayout deviceTableLayout = (TableLayout) findViewById(R.id.deviceTable);
		LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // reset view
		deviceTableLayout.removeAllViews();

        // build new table
		Cursor cursor = deviceManager.getAll(0);
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			((TextView) findViewById(R.id.textViewTableLabel)).setText(R.string.no_devices); // no devices paired
		}else{
			((TextView) findViewById(R.id.textViewTableLabel)).setText(R.string.devices);
		    while (!cursor.isAfterLast()) {
				final String id = cursor.getString(0);
				final String name = cursor.getString(1);
				  
				TableRow tr;
				tr = (TableRow) li.inflate(R.layout.device_line,null);
				((TextView)tr.findViewById(R.id.textName)).setText(name);
				((TextView)tr.findViewById(R.id.textID)).setText(id);
				ToggleButton toggleButtonSilenced = (ToggleButton)tr.findViewById(R.id.buttonSilence);
				toggleButtonSilenced.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        serverCommunicator.silence(id, ((ToggleButton) v).isChecked());
                    }
                });
				toggleButtonSilenced.setChecked(sharedPref.getBoolean("d_s_" + id, false));
				/** Settings **/
				tr.findViewById(R.id.buttonSettings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.action)
                                .setItems(R.array.actionArray, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int selectedAction) {
                                        if (selectedAction == 0) {
                                            nameDevice(id, name, 0);
                                        } else {
                                            deviceManager.delete(id, 0);
                                            reBuildDeviceTable();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    }
                });
				deviceTableLayout.addView(tr);
				cursor.moveToNext();
		    }
		}
		cursor.close();
		
	}
	/**
	 * Table with remote devices with permission to control volume of this device
	 */
	public void reBuildDeviceAccessTable(){
		TableLayout deviceTable = (TableLayout) findViewById(R.id.deviceAccesTable);
		LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		deviceTable.removeAllViews();
		Cursor cursor = deviceManager.getAll(1);
		cursor.moveToFirst();
		if(cursor.isAfterLast()){
			((TextView) findViewById(R.id.textViewTableAccesLabel)).setText(R.string.no_devices_acces);
		}else{
			((TextView) findViewById(R.id.textViewTableAccesLabel)).setText(R.string.devices_acces);
		    while (!cursor.isAfterLast()) {
		      final String id = cursor.getString(0);
		      final String name = cursor.getString(1);
		      boolean allowed = cursor.getInt(2)==1;
		      
		      TableRow tr;
				tr = (TableRow) li.inflate(R.layout.device_acces_line,null);
				((TextView)tr.findViewById(R.id.textName)).setText(name);
				((TextView)tr.findViewById(R.id.textID)).setText(id);
				ToggleButton tb = (ToggleButton)tr.findViewById(R.id.buttonAllow);
				tb.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						deviceManager.changePermission(id, ((ToggleButton) v).isChecked());
					}
				});
				tb.setChecked(allowed);
				tr.findViewById(R.id.buttonSettings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.action)
                                .setItems(R.array.actionArray, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int selectedOption) {
                                        if (selectedOption == 0) {
                                            nameDevice(id, name, 1);
                                        } else {
                                            deviceManager.delete(id, 1);
                                            reBuildDeviceAccessTable();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    }
                });
				deviceTable.addView(tr);
		      cursor.moveToNext();
		    }
		}
		cursor.close();
	}


    /**
     * Set device name dialog
     * @param id String id
     * @param name old name
     * @param i 0-device 1-permission
     */
	private void nameDevice(String name,final String id,final int i) {
		AlertDialog.Builder editAlert = new AlertDialog.Builder(this);
		editAlert.setTitle(R.string.get_name);
		final EditText newName = new EditText(this);
		newName.setTransformationMethod(new SingleLineTransformationMethod());
		newName.setText(name);
		newName.setSelectAllOnFocus(true);
		editAlert.setView(newName);
		editAlert.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceManager.rename(id, newName.getText().toString(), i);
                reBuildDeviceTable();
                reBuildDeviceAccessTable();
            }
        });
		editAlert.setNegativeButton(android.R.string.cancel, null);
		editAlert.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater=getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		registerReceiver(mHandleMessageReceiver,new IntentFilter(DISPLAY_MESSAGE_ACTION));
		registerReceiver(mHandleMessageReceiver,new IntentFilter(DISPLAY_REFRESH_UI));
		deviceManager = new DeviceManager(this);
		//deviceManager.open();
		reBuildDeviceTable();
		reBuildDeviceAccessTable();
	}
	protected void onPause(){
		unregisterReceiver(mHandleMessageReceiver);
		deviceManager.close();
		super.onPause();
	}
	/*public void onClickSilence(View v){
		silence_this();
	}
	
	public void silence_this(){
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		Log.wtf(TAG,"SILENCE");
		if(audioManager.getRingerMode()!= AudioManager.RINGER_MODE_SILENT){
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            toastMessage("Device remotely silenced");
        }
	}*/

    /**
     * Show message to user
     * @param text String to be shown
     */
    /*public void toastMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    */

    /*
    public void onClickServiceSwitch(View v){
		ToggleButton tb = (ToggleButton) v;
		if(tb.isChecked()){

		}else{

		}
	}*/

    /**
     * Action bar handler
     * @param item clicked item
     * @return boolean relevant
     */
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.action_about:
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ScrollView msg = (ScrollView) mInflater.inflate(R.layout.message, null);
                TextView fulltext = (TextView) msg.findViewById(R.id.message_text);
                fulltext.setText(R.string.info_msg);
                TextView text = (TextView) msg.findViewById(R.id.version);
                text.setText(context.getString(R.string.version) + " " + getAppVersion());

                CommonUtilities.doDialog(R.string.info_title, msg, null, context);
				return true;
			case R.id.action_get_pc:
				serverCommunicator.getPairKey();
				return true;
			case R.id.action_add_device:
				final EditText pairCode = new EditText(this);
				pairCode.setInputType(InputType.TYPE_CLASS_NUMBER);
				OnClickListener onClick = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int num) {
						serverCommunicator.pairWith(pairCode.getText().toString());
					}
				};
                CommonUtilities.doDialog(R.string.pairingcode, pairCode, onClick, context);
				return true;
			case R.id.action_show_log:
				final ScrollView scroll = new ScrollView(this);
				TextView tx = new TextView(this);
				tx.setText(sharedPref.getString("log", ""));
				scroll.addView(tx);
                CommonUtilities.doDialog(R.string.action_log, scroll, null,context);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

    /**
     * Handle additional GCM messages
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            String from = intent.getExtras().getString(MESSAGE_FROM);
            Log.i("GCM message", newMessage);
            for (String key : intent.getExtras().keySet()) {
                if (key.equals("from")) continue;
                Object value = intent.getExtras().get(key);
                String val = value.toString();
                String vclass = value.getClass().getName();
                Log.d("Message params", String.format("%s : %s (%s)", key, val, vclass));
            }
            if (intent.getAction().equals(CommonUtilities.DISPLAY_MESSAGE_ACTION)) {
                // WakeLocker.acquire(this);

                /*if (newMessage.equals("Your device registered with GCM")) {
                    regId = getRegistrationId(context);
                    serverCommunicator.setRegId(regId);
                    serverCommunicator.register();
                } else */
                if (from != null) {
                    if (newMessage.equals("addok")) {                                                    // after pairing
                        if (pairCodeDialog != null) pairCodeDialog.dismiss();
                        final EditText name = new EditText(context);
                        final String device_id = from;
                        name.setTransformationMethod(new SingleLineTransformationMethod());
                        OnClickListener onClick = new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deviceManager.addPermission(device_id, name.getText().toString());
                                reBuildDeviceAccessTable();
                            }
                        };
                        CommonUtilities.doDialog(R.string.nameit, name, onClick, context);
                    } else {
                        Toast.makeText(context, "New Message: " + newMessage + "From:" + from, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "New Message: " + newMessage, Toast.LENGTH_LONG).show();
                }
                // Releasing wake lock
                // WakeLocker.release();
            } else if (intent.getAction().equals(CommonUtilities.DISPLAY_REFRESH_UI)) {
                if (newMessage.equals("reBuildDeviceTable")) {
                    reBuildDeviceTable();
                }
            }
        }
    };


}
