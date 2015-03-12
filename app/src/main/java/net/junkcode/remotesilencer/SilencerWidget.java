package net.junkcode.remotesilencer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SilencerWidget extends AppWidgetProvider {
    public static String MY_WORKAROUND = "MY_OWN_WORKAROUND";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("onUpdate", "onUpdate called");
        for (int widgetId : appWidgetIds) {
            Log.d("onUpdate", "id: " + widgetId);
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			views.setTextViewText(R.id.widgetButtonOn, sharedPref.getString("w_"+appWidgetId,"name"));
			views.setTextViewText(R.id.widgetButtonOff, sharedPref.getString("w_"+appWidgetId,"name"));
			boolean on = sharedPref.getBoolean("w_s_"+appWidgetId, true);
			//views.setTextViewCompoundDrawables(R.id.widgetButton,0,(on?R.drawable.ico_sound:R.drawable.ico_mute),0,0);
			views.setViewVisibility(R.id.widgetButtonOn, (on?View.VISIBLE:View.GONE));
			views.setViewVisibility(R.id.widgetButtonOff, (!on?View.VISIBLE:View.GONE));
			Intent intent = new Intent(context, SilencerWidget.class);
			intent.setAction(SilencerWidget.MY_WORKAROUND);
			intent.putExtra("WID", appWidgetId);
			//intent.putExtra("remoteViews", views);
		    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,0);
		      
		    views.setOnClickPendingIntent(R.id.widgetButtonOn, pendingIntent);
			views.setOnClickPendingIntent(R.id.widgetButtonOff, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}
	  
	  @Override
	  public void onReceive(Context context, Intent intent){
		  super.onReceive(context, intent);
		  //Log.d("onReceiver()", "action: "+intent.getAction());
		  if(MY_WORKAROUND.equals(intent.getAction())){
			  Bundle extras = intent.getExtras(); 
			  if(extras!=null) {
				  AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				  int id = extras.getInt("WID");
				  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
				  boolean on = sharedPref.getBoolean("w_s_"+id, true);
				  
				  ServerCommunicator sc = new ServerCommunicator(context);
				  sc.silenceByWidget(id,on);
				  
				  sharedPref.edit().putBoolean("w_s_"+id, !on).apply();
				  updateAppWidget(context, appWidgetManager, id);
			  }
				 
			  //Toast.makeText(context, "onReceiver() - my create!", Toast.LENGTH_LONG).show();
		  }
		  

/*
	      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
	          R.layout.widget_layout);
	      Log.w("WidgetExample", String.valueOf(number));
	      // Set the text
	      remoteViews.setTextViewText(R.id.textView1, String.valueOf(number));

	      // Register an onClickListener
	      Intent intent = new Intent(context, SilencerWidget.class);

	      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

	      PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	          0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	      remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    //}*/
	    
	  }
	  /**
	    * Utility method to ensure that when we want an Intent that fire ACTION_APPWIDGET_UPDATE, the extras are correct.<br>
	    * The default implementation of onReceive() will discard it if we don't add the ids of all the instances.
	    * @param context
	    * @return
	    */
	   protected Intent get_ACTION_APPWIDGET_UPDATE_Intent(Context context){
	      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	      ComponentName thisAppWidget = new ComponentName(context.getPackageName(),SilencerWidget.class.getName());
	      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
	      Intent intent = new Intent(context, SilencerWidget.class);
	      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
	      return intent;
	   }
	    
}
