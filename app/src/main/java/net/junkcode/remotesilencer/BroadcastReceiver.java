package net.junkcode.remotesilencer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Button on 2.2.2015.
 * Contact: Button@junkcode.net
 * original source: https://code.google.com/p/gcm/source/browse/gcm-client/GcmClient/src/main/java/com/google/android/gcm/demo/app/GcmBroadcastReceiver.java
 */
public class BroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
        //    startWakefulService(context, intent)
        //}else {
            ComponentName comp = new ComponentName(context.getPackageName(), CustomIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
        //}
        if (!intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
            setResultCode(Activity.RESULT_OK);
        }
    }
}
