package com.example.tim.kellyscontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tim on 01/12/2015.
 */
public class AddContactActivity extends Activity{
    public EditText nameIn;
    public EditText emailIn;
    public EditText mobileIn;
    public EditText homeIn;
    public EditText addressIn;
    public Button addContact;
    private String jsonString;
    String name;
    String email;
    String mobile;
    String home;
    String address;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        //assign UI elements to objects
        nameIn = (EditText)findViewById(R.id.nameIn);
        emailIn = (EditText)findViewById(R.id.emailIn);
        mobileIn = (EditText)findViewById(R.id.mobileIn);
        homeIn = (EditText) findViewById(R.id.homeIn);
        addressIn = (EditText) findViewById(R.id.addressIn);
        addContact = (Button) findViewById(R.id.addContact);

        //listener on add contact button to do AddTask
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set variables as contents of textfields
                name = nameIn.getText().toString();
                email = emailIn.getText().toString();
                mobile = mobileIn.getText().toString();
                home = homeIn.getText().toString();
                address = addressIn.getText().toString();
                //check that the user has input a name for the contact
                if (!(name.isEmpty())) {
                    try {
                        //call AddTask class and store result in jsonString
                        jsonString = new AddTask().execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    //popup saying contact could not be created, return to main activity
                    CharSequence popup = "Name was null, contact not created";
                    Context context = getApplicationContext();
                    Toast toast =Toast.makeText(context, popup, Toast.LENGTH_SHORT);
                    toast.show();
                    Intent mainAct = new Intent(AddContactActivity.this, MainActivity.class);
                    mainAct.putExtra("First", false);
                    startActivity(mainAct);
                }
            }
        });
    }
    //creates a string which is parseable by the PHP script via encoding in UTF-8
    private String appendString (){
        //string builder to create string to append to end of web address for GET
        StringBuilder getAppend = new StringBuilder("?");
        getAppend.append("name=");
        //encoding to ensure fields are parseable by http
        String encodedName = "";
        try {
            encodedName = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedName);
        getAppend.append("&");
        getAppend.append("email=");
        getAppend.append(email);
        getAppend.append("&");
        getAppend.append("mobile=");
        String encodedMobile="";
        try {
            encodedMobile= URLEncoder.encode(mobile, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedMobile);
        getAppend.append("&");
        getAppend.append("home=");
        String encodedHome = "";
        try {
            encodedHome = URLEncoder.encode(home, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedHome);
        getAppend.append("&");
        getAppend.append("address=");
        String encodedAdr = "";
        try {
            encodedAdr = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //convert StringBuilder to String
        getAppend.append(encodedAdr);
        String toReturn = getAppend.toString();
        return toReturn;
    }
    //addTask accesses the add_contact php script and attempts to add the contact to the database
    private class AddTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){
            //call appendString method to encode all GET parameters
            String strURL = "http://31.51.178.61:8080/KellysDB/create_contact.php"+appendString();
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                //Convert String to URL
                url = new URL(strURL);
                //Create and open HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                //create bufferedreader and inputstream to read in response from script
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //read in response line by line
                while ((line = br.readLine()) !=null){
                    //append each line to the StringBuffer
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                AddContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(AddContactActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            //convert StringBuffer to String for return
            String jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            Context context = getApplicationContext();
            CharSequence popup = "";
            //check if result is success or failure to determine pop up
            //show pop up of whether contact creation was successful or not
            if(result.equalsIgnoreCase("{\"success\":0, \"message\":\"required field is missing\"}")|| result.equalsIgnoreCase("{\"success\":0, \"message\":\"an error occurred\"}")){
                popup= "Contact could not be created";
            } else {
                popup= "Contact Successfully Created";
            }
            int duration = Toast.LENGTH_SHORT;
            Toast toast =Toast.makeText(context, popup, duration);
            toast.show();
            //create and execute intent, return to mainActivity
            Intent mainAct = new Intent(AddContactActivity.this, MainActivity.class);
            mainAct.putExtra("First", false);
            startActivity(mainAct);
        }
    }
}
