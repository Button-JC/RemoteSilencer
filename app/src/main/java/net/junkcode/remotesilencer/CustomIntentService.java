package net.junkcode.remotesilencer;

import static net.junkcode.remotesilencer.CommonUtilities.displayMessage;
import static net.junkcode.remotesilencer.CommonUtilities.setSilence;

import java.util.Date;
import java.util.Set;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;


public class CustomIntentService extends IntentService {

    private static final String TAG = "GCMIntentService";

    /*public GCMIntentService(String name) {
        super(name);
    }*/

    public CustomIntentService() {
        super("ReminderService");
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void onError(Context context, String error) {
        Log.e(TAG, "GCM error:" + error);
        displayMessage(context, error, null);

    }

    /**
     * Method called on Receiving a new message
     */
    protected void onMessage(Context context, Intent intent) {

        String message = intent.getExtras().getString("message");
        String from = intent.getExtras().getString("from_id");
        Log.i(TAG, "Received message:" + intent.getExtras().toString());
        if (message.equals("Silence!") || message.equals("Silence stop")) {
            DeviceManager dm = new DeviceManager(this);
            //dm.open();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String log = sharedPref.getString("log", "");
            if (log.length() > 1000) log = log.substring(0, 999) + " ...";

            String date = (new Date()).toLocaleString();
            String newLog = date + "\n  MSG: " + message + " From: ";
            DeviceManager.ControlPermission perm = dm.getPermissionById(from);
            dm.close();
            if (perm == null) {
                newLog += "Unknown";
            } else {
                newLog += perm.name;
                if (perm.allowed) {
                    setSilence(context, message.equals("Silence!"));
                } else {
                    newLog += "(Ignored)";
                }
            }
            log = newLog + "\n\n" + log;
            sharedPref.edit().putString("log", log).apply();
        } else {
            displayMessage(context, message, from);
        }
        // notifies user
        //generateNotification(context, message);
    }

    /**
     * Method called on device registered
     **/
    /*protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "Your device registered with GCM",null);
        //Log.d("NAME", MainActivity.name);
        //ServerUtilities.register(context, MainActivity.name, MainActivity.email, registrationId);
    }*/

    /**
     * Method called on device unregistered
     */
    /*protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered),null);
        //ServerUtilities.unregister(context, registrationId);
    }*/

    /*@Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId),null);
        return super.onRecoverableError(context, errorId);
    }*/
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            changedInternally(this, intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1) == AudioManager.RINGER_MODE_SILENT);
        }else {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            // The getMessageType() intent parameter must be the intent you received
            // in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                /*
                 * Filter messages based on message type. Since it is likely that GCM will be
                 * extended in the future with new message types, just ignore any message types you're
                 * not interested in, or that you don't recognize.
                 */
                /*if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    onError(this,"Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    onError(this,"Deleted messages on server: " + extras.toString());
                } else*/
                if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    this.onMessage(this, intent);
                }
            }
            // Release the wake lock provided by the WakefulBroadcastReceiver.
        }
        BroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Triggers update of all widgets tied to local volume
     * @param context app context
     * @param silent true if device is muted
     */
    private void changedInternally(Context context, boolean silent) {
        Log.wtf(TAG,"local silence state: "+silent);
        DeviceManager dm  = new DeviceManager(context);
        Set<String>   widgetsIds = dm.getWidgetsTiedToLocalVolume();

        int[] intIds = new int[widgetsIds.size()];
        int i = 0 ;
        for(String id:widgetsIds){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPref.edit().putBoolean("w_s_"+id, !silent).apply();
            Log.wtf(TAG,"need to update widget: "+id);
            intIds[i++]=Integer.parseInt(id);
        }

        Intent intent = new Intent(context,SilencerWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intIds);
        sendBroadcast(intent);
    }
}
