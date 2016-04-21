package com.example.tim.kellyscontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tim on 19/11/2015.
 */
public class EditContactActivity extends Activity{
    public EditText name;
    public EditText email;
    public EditText mobile;
    public EditText home;
    public EditText address;
    public Button updateContact;
    public Button deleteContact;
    private String jsonLine;
    private String deleteString;
    private String updateString;
    private String getName;
    private String getEmail;
    private String getMobile;
    private String getHome;
    private String getAddress;
    String passedName;
    String passedID;
    String passedEmpID;
    //override onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //set layout to edit contact xml
        setContentView(R.layout.edit_contact);
        //assign UI elements to Objects
        name = (EditText) findViewById(R.id.inputName);
        email = (EditText) findViewById(R.id.inputEmail);
        mobile = (EditText) findViewById(R.id.inputMobile);
        home = (EditText) findViewById(R.id.inputHome);
        address = (EditText) findViewById(R.id.inputAddress);
        updateContact = (Button) findViewById(R.id.btnSave);
        deleteContact = (Button) findViewById(R.id.btnDelete);
        //get passed data from intent mainactivity
        //name of contact
        passedName = getIntent().getStringExtra("Name");
        Log.d("passed name", passedName);
        name.setText(passedName);
        //id of contact
        passedID = getIntent().getStringExtra("id");
        Log.d("passed ID", passedID);
        //employee ID
        passedEmpID = getIntent().getStringExtra("empID");

        String jsonString = jsonLine;
        //intialise modified retrieve task to get json of specific contact
        try {
            jsonString = new RetrieveTask().execute().get();
        } catch (InterruptedException e) {
            Toast.makeText(EditContactActivity.this, "No Connection, check database status", Toast.LENGTH_SHORT);
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String logString = "";
        //add LOG to contactlog table
            try {
                logString = new logToDB().execute().get();
            } catch (InterruptedException e) {
                Toast.makeText(EditContactActivity.this, "No Connection, check database status", Toast.LENGTH_SHORT);
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        //create jsonobject from retrievetask returned string
        try{
            JSONObject contact = new JSONObject(jsonString);
            Log.d("jsonObj", contact.toString());
            //trim JSONObject and add data to EditTexts
            email.setText(contact.getString("Email_Address"));
            mobile.setText(contact.getString("Mobile_Number"));
            home.setText(contact.getString("Home_Number"));
            address.setText(contact.getString("Address"));
        } catch (JSONException e){
            e.printStackTrace();
        }

        //create onClickListener for delete contact button
        deleteContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //create popup dialog to confirm deleting contact
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditContactActivity.this);
                alertDialog.setTitle("Confirm Dialog");
                alertDialog.setMessage("Are you sure you want to delete this contact?");
                //set 'no' option popup
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Contact not deleted", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
                //'yes' option logic, call deletetask class and store result in deletestring
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            deleteString = new DeleteTask().execute().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        //create popup
                        Toast.makeText(getApplicationContext(), "Contact will be deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                        alertDialog.show();
            }
        });

        //create onClickListener for save changes button
        updateContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //store editText data in variables
                    getName = name.getText().toString();
                    getEmail = email.getText().toString();
                    getMobile = mobile.getText().toString();
                    getHome = home.getText().toString();
                    getAddress = address.getText().toString();
                    //call updateTask and store result in updateString
                    updateString = new  UpdateTask().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });



    }
    //build string to append to HTTP GET for updating contact
    private String appendString(){
        StringBuilder getAppend = new StringBuilder("?");
        getAppend.append("id=");
        getAppend.append(passedID.toString());
        getAppend.append("&");
        getAppend.append("name=");
        //encoding to ensure fields are parseable by http
        String encodedName = "";
        try {
            encodedName = URLEncoder.encode(getName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedName);
        getAppend.append("&");
        getAppend.append("email=");
        getAppend.append(getEmail);
        getAppend.append("&");
        getAppend.append("mobile=");
        String encodedMobile="";
        //encoding to ensure fields are parseable by http
        try {
            encodedMobile= URLEncoder.encode(getMobile, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedMobile);
        getAppend.append("&");
        getAppend.append("home=");
        String encodedHome = "";
        //encoding to ensure fields are parseable by http
        try {
            encodedHome = URLEncoder.encode(getHome, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedHome);
        getAppend.append("&");
        getAppend.append("address=");
        String encodedAdr = "";
        //encoding to ensure fields are parseable by http
        try {
            encodedAdr = URLEncoder.encode(getAddress, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedAdr);
        return getAppend.toString();
    }
    //modified retrievetask uses HTTP GET to pass ID to php script and return the response as a string
    private class RetrieveTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){

            //choosing which php script to access
            String strURL = "http://31.51.178.61:8080/KellysDB/get_contact_details.php?id="+passedID;
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                //converting String to URL
                url = new URL(strURL);
                //create HttpURLConnection and open
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                //create bufferedreader and inputstreamreader to parse data from the php script
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //read json response line by line
                while ((line = br.readLine()) !=null){
                    //store each line in stringBuffer
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                EditContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EditContactActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            //convert stringBuffer to String
            jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("some tag", result);

        }
    }
    //modified retrieveTask to update contact and return success value
    private class UpdateTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){
            //call appendString and store created string in
            String toGET = appendString();
            //choose php script and append GET params
            String strURL = "http://31.51.178.61:8080/KellysDB/update_contact.php"+toGET;
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                //convert string to URL
                url = new URL(strURL);
                //create HttpURLConnection and open
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                //create bufferedreader and inputstreamreader to parse json data
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //read in json response line by line
                while ((line = br.readLine()) !=null){
                    //store each line in stringBuffer
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                EditContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EditContactActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            //convert stringBuffer to String for return
            jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("Update tag", result);
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            String bread = "";
            //TODO fix this
            //check if contact wsa successful and determine popup content

            if (result.contains("1")){
                bread = "Contact Updated";
            } else {
                bread = "Contact could not be updated";
            }
            //create popup and create intent to return to MainActivity
            Toast toast = Toast.makeText(context, bread, duration);
            toast.show();
            Intent mainAct = new Intent(EditContactActivity.this, MainActivity.class);
            mainAct.putExtra("First", false);
            startActivity(mainAct);
        }
    }
    // modififed retrieveTask to delete selected contact
    private class DeleteTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){
            //choose php script and append contact ID
            String strURL = "http://31.51.178.61:8080/KellysDB/delete_contact.php?id="+passedID;
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                //convert String to URL
                url = new URL(strURL);
                //create HttpURLConnection and connect
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                //create bufferedreader and inputstreamreader to parse json response
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //read json response line by line
                while ((line = br.readLine()) !=null){
                    //store each line in StringBuffer
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                EditContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EditContactActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
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
            Log.d("some tag", result);
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            String bread = "";
            //determine popup text from JSON return
            if ((result.contains("1"))){
                bread="Contact Deleted";

            } else {
                bread= "Contact could not be deleted";
            }
            //create popup and Intent to return to MainActivity
            Toast toast = Toast.makeText(context, bread, duration);
            toast.show();
            Intent mainAct = new Intent(EditContactActivity.this, MainActivity.class);
            mainAct.putExtra("First", false);
            startActivity(mainAct);

        }
    }
    //creates GET string for Log
    private String appendStringLog (){
        StringBuilder getAppend = new StringBuilder("?");
        getAppend.append("contactID=");
        getAppend.append(passedID);
        getAppend.append("&");
        getAppend.append("employeeID=");
        getAppend.append(passedEmpID);
        return getAppend.toString();
    }
    //modified retrieveTask to add LOG of contact being viewed
    private class logToDB extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String toReturn;
            String toAppend = appendStringLog().toString();
            Log.d("GET STRING", toAppend);
            String strURL = "http://31.51.178.61:8080/KellysDB/create_log.php"+ toAppend;
            URL url;
            StringBuffer sb = new StringBuffer();
            try {
                url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                EditContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EditContactActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            toReturn = sb.toString();
            return toReturn;
        }
        @Override
        protected void onPostExecute (String result){
            super.onPostExecute(result);
            Log.d("Returned message Log:", result);
        }
    }
}
