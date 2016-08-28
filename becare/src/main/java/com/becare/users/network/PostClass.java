package com.becare.users.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;


import com.becare.users.data.UploadDataHelper;

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
public class PostClass extends AsyncTask<String, Void, Void> {
        private final Context context;
        private TextView statusText;
        private UploadDataHelper uploadDataHelper;
        private ProgressDialog progress;
        private boolean status = false;

        public PostClass(Context c, TextView t, UploadDataHelper data){
            this.context = c;
            this.statusText = t;
            this.uploadDataHelper = data;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                URL url = new URL(HiveHelper.INSERT_URL);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);


                String str =  uploadDataHelper.getUploadDataStr(System.currentTimeMillis());
                byte[] outputInBytes = str.getBytes("UTF-8");
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

                status = true;
            } catch (MalformedURLException e) {
                status =false;
                e.printStackTrace();
            } catch (IOException e) {
                status = false;
                e.printStackTrace();
            }
            return null;
        }

    @Override
    protected void onPostExecute(Void str){
        statusText.setVisibility(View.VISIBLE);
        if(status)
            statusText.setText("Uploaded Successfully :)");
        else
            statusText.setText("Upload Failed :(");
        progress.hide();
    }

}
