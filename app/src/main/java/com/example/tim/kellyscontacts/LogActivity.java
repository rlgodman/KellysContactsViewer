package com.example.tim.kellyscontacts;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tim on 05/01/2016.
 */
public class LogActivity extends Activity {

    public TextView displayText;
    private String jsonLine;
    String passedID;
    String passedEmpID;
    //Override onCreate method
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //set layout to log_activity xml
        setContentView(R.layout.log_activity);
        //assign GUI elements to objects
        displayText = (TextView) findViewById(R.id.logView);
        //make text view scrollable so all LOGS can be seen
        displayText.setMovementMethod(new ScrollingMovementMethod());
        //fix passed ID as arrays start a 0
        passedID = getIntent().getStringExtra("id");
        passedEmpID = getIntent().getStringExtra("empID");

       //get contact details via retrievetask and store in jsonString
        String jsonString = "";
        try {
        jsonString = new RetrieveTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            //create JSONObject of contact and display in textfield
            JSONObject contact = new JSONObject(jsonString);
            String allContact = "Name=" + contact.getString("Name")+"\n";
            allContact = allContact + "Email=" + contact.getString("Email_Address")+"\n";
            allContact = allContact + "Mobile=" + contact.getString("Mobile_Number")+"\n";
            allContact = allContact + "Home=" + contact.getString("Home_Number")+"\n";
            allContact = allContact + "Address=" + contact.getString("Address")+"\n";

            displayText.setText(allContact);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //call retrieveLog and store result in logString
        String logString ="";
       try {
            logString = new RetrieveLog().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //write a blank line to text field
        displayText.append("\n");
        //create JSONArray from logString
        try {
            JSONArray logJA = new JSONArray(logString);
            //create JSONObject for every log entry and append the information to the Text Field
            for(int i = 0; i< logJA.length(); i++){
                JSONObject temp = logJA.getJSONObject(i);
                displayText.append(temp.getString("Name") + " Contacted this person on: ");
                displayText.append(temp.getString("date_time")+ "\n");

            }
            if (logJA.toString().equalsIgnoreCase("null")){
                displayText.append("No Logs for this contact!");
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    //retrieveTask gets contact details from the php script and returns them as a string
    private class RetrieveTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){

            //The url of the script to access with HTTP GET appended
            String strURL = "http://31.51.178.61:8080/KellysDB/get_contact_details.php?id="+passedID;
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) !=null){
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                LogActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LogActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

        }
    }
    //RetrieveLog retrieves all log entries associated with the contact that is being viewed
    //returns the data in a string
    private class RetrieveLog extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){

            //url of php script with HTTP GET appended
            String strURL = "http://31.51.178.61:8080/KellysDB/get_all_logs.php?contactID="+passedID;
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                //convert String to url object
                url = new URL(strURL);
                //create and open HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                //Create inputStream and BufferedReader to read in the return from the php script
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //append String for every line to StringBuffer
                while ((line = br.readLine()) !=null){
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                LogActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LogActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            //convert StringBuffer to String for return
            jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

        }
    }
}
