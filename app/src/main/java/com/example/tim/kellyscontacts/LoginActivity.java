package com.example.tim.kellyscontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tim on 05/01/2016.
 */
public class LoginActivity extends Activity {
    public Button submit;
    public Button newAcc;
    public EditText enterPass;
    public EditText enterName;
    private String submitName;
    private String submitPassword;
    private String empIDToPass;
    private String createString;
    private String checkString;
    private String toReturn;
    private JSONArray employeesJA;

    //override onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.login_activity);
        //assign GUI elements to objects
        newAcc = (Button) findViewById(R.id.signUpBtn);
        submit = (Button) findViewById(R.id.submitBtn);
        enterName = (EditText) findViewById(R.id.enterName);
        enterPass = (EditText) findViewById(R.id.enterPassword);
        //create arraylists to store employee data for login checks
        final ArrayList<String> employees = new ArrayList<>();
        final ArrayList<String> employeeUID = new ArrayList<>();

        //create onclicklistener for submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store name and password from text fields in variables
                submitName = enterName.getText().toString();
                submitPassword = enterPass.getText().toString();
                //call isnetworkavailable to check if wifi or mobile data can connect to the internet
                if (!isNetworkConnected()){
                    Toast.makeText(getApplicationContext(), "No Connection to Network, check WIFI/Mobile Data", Toast.LENGTH_SHORT).show();
                }
                //retrieve employee usernames/passwords
                final ArrayList<String> employees = new ArrayList<>();
                try {
                    checkString = new RetrieveTask().execute().get();

                } catch (InterruptedException e) {
                    Toast.makeText(LoginActivity.this, "No Connection, check database status", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    //store each pair in JSONArray
                    employeesJA = new JSONArray(checkString);
                    for (int i = 0; i < employeesJA.length(); i++) {
                        //split JSONArray into JSONObjects
                        JSONObject temp = employeesJA.getJSONObject(i);
                        //fill arraylist with Name portion of JSON response
                        employees.add(temp.getString("Name"));
                        employees.add(temp.getString("Password"));
                        employeeUID.add(temp.getString("id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //set up variable which is modified if matching employee found
                Boolean match = false;
                for (int i = 0; i < employees.size(); i++) {
                    //check if TextField values match any from employees table
                    if (submitName.equals(employees.get(i)) && submitPassword.equals(employees.get(i + 1))) {
                        match = true;
                        //create Intent to open MainActivity, passing the ID of the employee who has logged in.

                        empIDToPass = employeeUID.get(i / 2);
                        int duration = Toast.LENGTH_SHORT;
                        Context context = getApplicationContext();
                        Toast toast = Toast.makeText(context, "Loading Contacts...", duration);
                        toast.show();
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.putExtra("empID", empIDToPass);
                        //send True flag, so MainActivity knows it is being started by the LoginActivity
                        mainIntent.putExtra("First", true);
                        //add flags to LoginActivity so it is cleared from memory
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        //finish();
                    }
                }
                //if no match was found
                if (match == false) {
                    //create popup
                    int duration = Toast.LENGTH_SHORT;
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "These details have never been registered or are incorrect, try again. Press New Account if you have never registered.", duration);
                    toast.show();
                    match = true;

                }
            }

        });
        //set up onclicklistener for new account button
        newAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store name and password from text fields in variables
                submitName = enterName.getText().toString();
                submitPassword = enterPass.getText().toString();
                if (!isNetworkConnected()){
                    Toast.makeText(getApplicationContext(), "No Connection to Network, check WIFI/Mobile Data", Toast.LENGTH_SHORT).show();
                }
                //create employees arraylist to store employee details
                final ArrayList<String> employees = new ArrayList<>();
                try {
                    //run retrievetask and store results in checkString
                    checkString = new RetrieveTask().execute().get();

                } catch (InterruptedException e) {
                    Toast.makeText(LoginActivity.this, "No Connection, check database status", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    //create JSONArray and populate with checkString
                    employeesJA = new JSONArray(checkString);
                    for (int i = 0; i < employeesJA.length(); i++) {
                        //create JSONObject for each employee and store Name and Password fields in arrayList
                        JSONObject temp = employeesJA.getJSONObject(i);
                        //fill arraylist with Name portion of JSON response
                        employees.add(temp.getString("Name"));
                        employees.add(temp.getString("Password"));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //create boolean to be modified if matching details found in table
                Boolean match = false;
                //checks to make sure username and password are not the same
                if (submitName.equalsIgnoreCase(submitPassword)) {
                    match = true;
                    //create popup telling user why 'New Account' Failed
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "The Username and Password cannot be the same.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                for (int i = 0; i < employees.size(); i++) {
                    //check if TextFields values match any employees
                    if (submitName.equals(employees.get(i)) && submitPassword.equals(employees.get(i + 1))) {
                        Log.d("all employees", employees.get(i));

                        //if details are matched, send popup
                        int duration = Toast.LENGTH_SHORT;
                        Context context = getApplicationContext();
                        Toast toast = Toast.makeText(context, "These details have already been registered, press Log In instead!", duration);
                        toast.show();
                        match = true;
                    }
                    //checks to see if name of employee trying to create account is unique
                    for (int j = 0; j < employees.size(); j += 2) {
                        if (submitName.equals(employees.get(j))) {
                            //if not unique, create and show popup
                            int duration = Toast.LENGTH_SHORT;
                            Context context = getApplicationContext();
                            Toast toast = Toast.makeText(context, "This name has already been registered, press Log In instead or try another name", duration);
                            toast.show();
                            match = true;
                        }
                    }


                }
                //if match still = false then create account and start MainActivity
                if (match == false) {
                    //call nameToDB class and store result in createString
                    try {
                        createString = new nameToDB().execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    //create intent to open MainActivity
                    int duration = Toast.LENGTH_SHORT;
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "Loading Contacts...", duration);
                    toast.show();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    //finish();
                }
            }
        });
    }
    //check if wifi/mobile data is on
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    //appendString creates the HTTP GET for nameToDB to append to the script URL
    private String appendString() {
        StringBuilder getAppend = new StringBuilder("?");
        getAppend.append("name=");
        String encodedName = "";
        //encode name so spaces or special characters do not cause error
        try {
            encodedName = URLEncoder.encode(submitName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedName);
        getAppend.append("&password=");
        String encodedPassword = "";
        //encode password so spaces or special characters do not cause error
        try {
            encodedPassword = URLEncoder.encode(submitPassword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getAppend.append(encodedPassword);
        return getAppend.toString();
    }

    //nameToDB calls the create_employee script and appends the input Name and Password, returns whether adding employee was successful or not
    private class nameToDB extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String toReturn;
            String toAppend = appendString();
            Log.d("GET STRING", toAppend);
            String strURL = "http://31.51.178.61:8080/KellysDB/create_employee.php" + toAppend;
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
            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            toReturn = sb.toString();
            return toReturn;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Returned message:", result);
        }
    }

    //retrieveTask gets all employees and stores them in a String to be manipulated by the main method
    private class RetrieveTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String toReturn = "";
            String strURL = "http://31.51.178.61:8080/KellysDB/get_all_employees.php";
            URL url;
            StringBuffer sb = new StringBuffer();
            try {
                url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                is.close();
                connection.disconnect();
                //catch timeout and create toast to tell user of error
            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            toReturn = sb.toString();
            return toReturn;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result); //Log.d("All Employees:", result);
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Employees Loaded", duration); //toast.show();
        }
    }

}

