package net.junkcode.remotesilencer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.view.View;

public final class CommonUtilities {

    // Google project id
    //static final String SENDER_ID = ;

    @SuppressWarnings("UnusedDeclaration")
    static final String TAG = "CommonUtilities";

    static final String DISPLAY_MESSAGE_ACTION = "net.junkcode.remotesilencer.DISPLAY_MESSAGE";

    static final String DISPLAY_REFRESH_UI = "net.junkcode.remotesilencer.REFRESH_UI";


    static final String EXTRA_MESSAGE = "message";
    static final String MESSAGE_FROM  = "from";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message, String from) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(MESSAGE_FROM, from);
        context.sendBroadcast(intent);
    }
    static void sendUIMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_REFRESH_UI);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    static void setSilence(Context context, boolean on){
    	AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(on){
			//if(audioManager.getRingerMode()!= AudioManager.RINGER_MODE_SILENT){
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				//am.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);  // TODO: make muting this optional
				//am.setStreamMute(AudioManager.STREAM_MUSIC, true);
				//am.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			//}
        }else{
        	//if(audioManager.getRingerMode()!= AudioManager.RINGER_MODE_NORMAL){
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				//am.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
				//am.setStreamMute(AudioManager.STREAM_MUSIC, false);
				//am.setStreamMute(AudioManager.STREAM_SYSTEM, false);
			//}
        }
    }

    /**
     * Generate simple dialog with message
     * @param title dialog title
     * @param message dialog message
     * @param context content to be associated with dialog
     * @return shown dialog
     */
    public static AlertDialog doSimpleDialog(String title,String message,Context context){
        AlertDialog.Builder infoAlert = new AlertDialog.Builder(context);

        infoAlert.setTitle(title);
        infoAlert.setMessage(message);
        infoAlert.setPositiveButton(android.R.string.ok, null);
        return infoAlert.show();
    }

    /**
     * Generate dialog with view and callback
     * @param title dialog title
     * @param view view to be displayed
     * @param onClick callback on OK click
     * @param context content to be associated with dialog
     */
    public static void doDialog(int title,View view, DialogInterface.OnClickListener onClick,Context context){
        AlertDialog.Builder infoAlert = new AlertDialog.Builder(context);
        infoAlert.setTitle(title);
        infoAlert.setView(view);
        infoAlert.setPositiveButton(android.R.string.ok,onClick);
        infoAlert.setNegativeButton(android.R.string.cancel, null);
        infoAlert.show();
    }
}
