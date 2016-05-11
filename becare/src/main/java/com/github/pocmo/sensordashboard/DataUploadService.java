package com.github.pocmo.sensordashboard;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by neerajpaliwal on 06/04/16.
 */
public class DataUploadService extends IntentService {

    private static final String TAG = "DataUploadService";
    public static final String EXTRA_POST_BOSY = "extra_post_body";

    public DataUploadService() {
        super("DataUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        String body = intent.getStringExtra(EXTRA_POST_BOSY);
        {
            try {

                URL url = new URL("http://hivedemo.qtxsystems.net/Api/DataApi.svc/Insert");

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);


                byte[] outputInBytes = body.getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(outputInBytes);
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                System.out.println("output===============" + br);
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                //SharedPreferences preferences = getApplicationContext().getSharedPreferences(SensorReceiverService.PREFS, Context.MODE_PRIVATE);
                //preferences.edit().putLong(SensorReceiverService.PREFS_LAST_UPDATED_TIME, System.currentTimeMillis()).commit();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
