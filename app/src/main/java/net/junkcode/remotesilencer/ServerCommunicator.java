package net.junkcode.remotesilencer;

import static net.junkcode.remotesilencer.CommonUtilities.sendUIMessage;
import static net.junkcode.remotesilencer.CommonUtilities.setSilence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

//import com.google.android.gcm.GCMRegistrar;

public class ServerCommunicator {

    private static final String PROPERTY_REG_ID = "RegistrationID";
    //private final CallBackInterface activityCallBackInterface;
    private String regId   = null;

    private GoogleCloudMessaging gcm;

    Context context;

    String shortId = "";
    String TAG     = "ServerCommunicator";
    private SharedPreferences sharedPref;

    public ServerCommunicator(Context context) {
        this.context = context;
        //this.activityCallBackInterface = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        shortId = sharedPref.getString("server_id", "");

        gcm = GoogleCloudMessaging.getInstance(context);
        regId = getRegistrationId();

        if (regId.isEmpty()) {
            registerInBackground();
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId() {
        String registrationId = sharedPref.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        /*
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }*/
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(BuildConfig.SENDER_ID);
                    msg = "Device registered";//, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    // sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(regId);
                } catch (IOException ex) {
                    msg = "Error: " + ex.getMessage()+" Try restarting the app.";
                    Log.e(TAG,msg);
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
                register();
            }
        }.execute(null, null, null);
    }


    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        //int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId "+regId);// "on app version " + appVersion);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        //editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
        sharedPref.edit().putString("server_id", shortId).apply();
    }

	public void register() {
		if(regId==null || regId.equals("")) {
			Toast.makeText(context, "Not registered with GCM service",Toast.LENGTH_LONG).show();
		}else{
			(new httpCommand()).execute("http://silencer.junkcode.net/api/trustie.php?request=register&device_addr="+regId);
		}
	}

    public boolean silence(String key, boolean b) {
        if (isRegistered()){
			sharedPref.edit().putBoolean("d_s_"+key, b).apply();
				if(b){
					(new httpCommand()).execute("http://silencer.junkcode.net/api/trustie.php?request=comm&type=silence&device_id="+key+"&device_id_from="+shortId);
				}else{
					(new httpCommand()).execute("http://silencer.junkcode.net/api/trustie.php?request=comm&type=silence_off&device_id="+key+"&device_id_from="+shortId);
				}
			return true;	
		}
		return false;
	}
	public void getPairKey() {
		if(isRegistered())(new httpCommand()).execute("http://silencer.junkcode.net/api/trustie.php?request=getpaircode&device_id="+shortId);
	}
	public void pairWith(String code) {
		if(isRegistered())(new httpCommand()).execute("http://silencer.junkcode.net/api/trustie.php?request=pair&pair_code="+code+"&from_id="+shortId);
	}
	
	
	public boolean isRegistered(){
		if(shortId.equals("")){
			Toast.makeText(context, "Not registered with server, try again later", Toast.LENGTH_LONG).show();
			register();
			return false;
		}else{
			return true;
		}
	}
	/*** HTTP COMM TO SERVER ***/
    class httpCommand extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
    		StringBuilder builder = new StringBuilder();
    	    HttpClient client = new DefaultHttpClient();  
    	    HttpGet get = new HttpGet(urls[0]);
    	    try {
  		      HttpResponse response = client.execute(get);
  		      StatusLine statusLine = response.getStatusLine();
  		      int statusCode = statusLine.getStatusCode();
  		      if (statusCode == 200) {
  		        HttpEntity entity = response.getEntity();
  		        InputStream content = entity.getContent();
  		        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
  		        String line;
  		        while ((line = reader.readLine()) != null) {
  		          builder.append(line);
  		        }
  		      } else {
  		        Log.e(MainActivity.class.toString(), "Failed to download file with status code "+statusCode);
  		      }
  		    } catch (ClientProtocolException e) {
  		       Log.e(TAG,"Failed to get response",e);
  		    } catch (IOException e) {
               Log.e(TAG,"Failed to get response",e);
  		    }
  			return builder.toString();
        }
        protected void onPostExecute(String resp) {
        	//Toast.makeText(this, "New Message: " + resp, Toast.LENGTH_LONG).show();
        	try {
				JSONObject answer = new JSONObject(resp);
				if(answer.getInt("success")==1){
					if(answer.getString("action").equals("comm")){
						//Toast.makeText(context, "Command sent", Toast.LENGTH_SHORT).show();
					}else if(answer.getString("action").equals("get_pc")){
						int code = answer.getInt("code"); 
						MainActivity.pairCodeDialog = CommonUtilities.doSimpleDialog(context.getString(R.string.pairingcode), Integer.toString(code), context);
					}else if(answer.getString("action").equals("reg")){
						setShortId(answer.getString("id"));
					}else if(answer.getString("action").equals("add_device")){
						final EditText name = new EditText(context);
						final String device_id = answer.getString("device_id");
						OnClickListener onClick = new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								DeviceManager dm = new DeviceManager(context);
								//dm.open();
								dm.add(device_id, name.getText().toString(),0);
								dm.close();
								//reBuildDeviceTable();
								sendUIMessage(context, "reBuildDeviceTable");
							}
						};
						CommonUtilities.doDialog(R.string.nameit, name, onClick, context);
					}else{
						Toast.makeText(context, "Unexpected answer from server", Toast.LENGTH_LONG).show();
						Log.e(TAG, "Unexpected answer from server: "+resp);
					}
				}else if(answer.getInt("success")==0){
					if(answer.has("error")){
						Toast.makeText(context, answer.getString("error"), Toast.LENGTH_LONG).show();
					}else if(answer.has("results") && answer.getJSONArray("results").getJSONObject(0).has("error")){
						String error = answer.getJSONArray("results").getJSONObject(0).getString("error");
						if(!error.equals("NotRegistered")){
							Toast.makeText(context, "GCM returned error", Toast.LENGTH_LONG).show();
							Log.e(TAG, "GCM returned error: "+error);
						}
					}
				}
			} catch (JSONException e) {
				if(resp.equals("")){
					Toast.makeText(context, "Server not responding", Toast.LENGTH_LONG).show();
				}else{
					Log.e(TAG, "Something went wrong: "+resp+" "+e.toString());
					Toast.makeText(context, "Something went wrong on the server", Toast.LENGTH_LONG).show();
				}
			}
        }
    }

	public void silenceByWidget(int d_id, boolean on) {
		DeviceManager dm = new DeviceManager(context);
		Set<String> ids = dm.getDevicesToSilentByWidgetId(d_id);
		for(String id : ids){
			if(id.equals("self")){
				sharedPref.edit().putBoolean("d_s_"+id, on).apply();
				setSilence(context,on);
			}else{
				silence(id, on);
			}
		}
		dm.close();
		
	}


	
}
