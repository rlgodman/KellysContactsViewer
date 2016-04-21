package com.example.tim.kellyscontacts;


import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    //setting up variables for with scope to be accessed by the whole activity
    public ListView list;
    //GUI Elements
    public Button addNew;
    public EditText searchBar;
    private String jsonLine;
    private int toPass;
    private String nameToPass;
    private JSONArray contactsJA;
    private ArrayAdapter<String> arrayAdapter;
    private int toReload=0;
    private int forLog;
    String passedEmpID;

    //override onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //choose layout to use for this activity
        setContentView(R.layout.activity_main);
        //assign listview from layout to list object
        list = (ListView) findViewById(R.id.list);
        //make list searchable
        list.setTextFilterEnabled(true);
        //set up long press menu on list items
        registerForContextMenu(list);
        //assign other UI elements to objects
        addNew = (Button) findViewById(R.id.btnGet);
        searchBar =(EditText) findViewById(R.id.searchBar);
        //get passedEmpID from LoginActivity
        //gets passed boolean from all Activities which can access MainActivity and only asks for EmpID if bookean is True
        Boolean firstLogin = getIntent().getBooleanExtra("First", false);
        if (firstLogin == true){
            passedEmpID = getIntent().getStringExtra("empID");
            Log.d("passed empID=", passedEmpID);
        }

        //open addcontactactivity on button press
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addCont = new Intent(MainActivity.this, AddContactActivity.class);
                toReload++;
                startActivity(addCont);
            }
        });

        //create ArrayList and populate with JSON get all contacts
        //call retrievetask and store returned values in jsonString
        String jsonString=jsonLine;
        final ArrayList<String> contacts = new ArrayList<>();
        try {
             jsonString = new RetrieveTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            //create jsonArray from values returned by retrieveTask
             contactsJA = new JSONArray(jsonLine);
            //store each separate JSONobject from JSONArray as a JSONObject - in this case each separate contact
            for(int i = 0; i< contactsJA.length(); i++){
                JSONObject temp = contactsJA.getJSONObject(i);
                //fill arraylist with Name portion of JSON response

                contacts.add(temp.getString("Name"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //arrayadapter to populate list
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts );
        list.setAdapter(arrayAdapter);

        //if list item clicked, open editcontactactivity and pass values via intent
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent = new Intent(MainActivity.this, EditContactActivity.class);
                //get list position of clicked contact and store in variable
                //gets the Name of the clicked contact
                String name = ((TextView) view).getText().toString();
                //sends name to get_contact_details2 to return ID
                nameToPass = name;
                String uid = "";
                //get unique ID of contact at clicked list position using return from get_contact_details2
                String getDetailsString = "";
                try {
                    getDetailsString= new RetrieveDetails2().execute().get();
                } catch (Exception e){
                    e.printStackTrace();
                }
                //create jsonobject from retrieveDetails returned string
                try{
                    JSONObject contact = new JSONObject(getDetailsString);
                    Log.d("jsonObj", contact.toString());
                    uid = contact.getString("id");

                } catch (JSONException e){
                    e.printStackTrace();
                }

                //pass values via intent
                editIntent.putExtra("id", uid);
                editIntent.putExtra("Name", name);
                editIntent.putExtra("empID", passedEmpID);
                toReload++;
                //open EditContactActivity
                if(uid.equalsIgnoreCase("")){
                    Toast.makeText(MainActivity.this, "No Connection, check Server status", Toast.LENGTH_LONG );

                } else {
                    startActivity(editIntent);
                }

            }
        });


        //searches list when textfield changes
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.this.arrayAdapter.getFilter().filter(s);
            }
        });
        //reloads contact list if one added or edited
        if (toReload>=1){
            //as commented above
            try {
                jsonString = new RetrieveTask().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            try {

                contactsJA = new JSONArray(jsonLine);
                for(int i = 0; i< contactsJA.length(); i++){
                    JSONObject temp = contactsJA.getJSONObject(i);
                    //fill arraylist with Name portion of JSON response
                    contacts.add(temp.getString("Name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //arrayadapter to populate list
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts );
            list.setAdapter(arrayAdapter);
        }

    }
    //creates menu to be displayed when contacts long pressed
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("What do you want to do?");
        menu.add(0, v.getId(), 0, "Call Mobile");
        menu.add(0, v.getId(), 0, "Call Home");
        menu.add(0, v.getId(), 0, "Email Contact");
        menu.add(0, v.getId(), 0, "Text Mobile");
        menu.add(0, v.getId(), 0, "View Log");

    }
    //runs clicked action
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        //get the name of the clicked item
        String name = ((TextView) info.targetView).getText().toString();
        //call the getTrueId and pass it the name
        toPass = getTrueID(name);
        String getDetailsString = "";
        try {
            getDetailsString= new RetrieveDetails().execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        //Initialise variables to pass
        String email = "error";
        String mobile = "0";
        String home = "0";
        int logID = 88888;
        //create jsonobject from retrieveDetails returned string
        try{
            JSONObject contact = new JSONObject(getDetailsString);
            Log.d("jsonObj", contact.toString());
            //retrieve ID, Mobile, Home and Email from the JSONObject
            String logString = contact.getString("id");
            logID = Integer.parseInt(logString);
            mobile = contact.getString("Mobile_Number");

            home = contact.getString("Home_Number");

            email = contact.getString("Email_Address");

        } catch (JSONException e){
            e.printStackTrace();
        }
        forLog = logID;
        //call specific function depending on which option the user chooses, passing the required variable
        if (item.getTitle()=="Call Mobile") {
           callMobile(mobile);
        } else if (item.getTitle()=="Call Home"){
           callHome(home);
        } else if (item.getTitle()=="Email Contact"){
           sendEmail(email);
        } else if (item.getTitle()=="Text Mobile"){
            sendSMS(mobile);
        } else if (item.getTitle()=="View Log"){
            startLog(logID);
        } else {return false;}
        return true;
    }
    //starts LogActivity
    private void startLog(int position){
        Intent openLog = new Intent(MainActivity.this, LogActivity.class);
        String toPass = Integer.toString(position);
        //contact ID passed to LogActivity via intent
        openLog.putExtra("id", toPass);
        //Employee ID passed to LogActivity via intent
        openLog.putExtra("empID", passedEmpID);
        toReload++;
        startActivity(openLog);
    }
    //opens Phone application with selected contact mobile number input
    private void callMobile (String mobile){
        //create log entry for the contact, unless no Contact ID was found
        if (forLog != 88888){
            createLogEntry();
        }
        //set up Mobile Number in correct format
        String phone = "tel:"+mobile;

        Log.d("PASSED Mobile", phone);
        //create callIntent which opens Dialer
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        //sets dialer to already have phone variable input
        callIntent.setData(Uri.parse(phone));
        startActivity(callIntent);
    }
    //opens Phone application with selected contact home number input
    private void callHome (String home){
        //create log entry for the contact, unless no Contact ID was found
        if (forLog != 88888){
            createLogEntry();
        }
        //set up Hom Number in correct format
        String phone = "tel:"+home;

        Log.d("PASSED Home", phone);
        //create callIntent which opens Dialer
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        //sets dialer to already have phone variable input
        callIntent.setData(Uri.parse(phone));
        startActivity(callIntent);
    }
    //opens email application with contact email address input in To: field
    private void sendEmail (String email){
        //create log entry for the contact, unless no Contact ID was found
        if (forLog != 88888){
            createLogEntry();
        }
        //create emailIntent and fills in address with email variable
        Intent emailIntent = new Intent (Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject of Email");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body of Email");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Mail"));
            finish();
        } catch (android.content.ActivityNotFoundException e){
            Toast.makeText(MainActivity.this, "No Email Client Installed", Toast.LENGTH_SHORT);
        }
    }
    //opens SMS application with selected contact mobile number input
    private void sendSMS (String mobile){
        //create log entry for the contact, unless no Contact ID was found
        if (forLog != 88888){
            createLogEntry();
        }
        Log.d("PASSED Mobile", mobile);
        //create SMSintent and fill to field with phone variable
        Intent SMSIntent = new Intent(Intent.ACTION_SENDTO);
        SMSIntent.addCategory(Intent.CATEGORY_DEFAULT);
        SMSIntent.setType("vnd.android-dir/mms-sms");
        SMSIntent.setData(Uri.parse("sms:" + mobile));
        startActivity(SMSIntent);
    }
    private int getTrueID (String name){

        //sends name to get_contact_details2 to return ID
        nameToPass = name;
        String uid = "";
        //get unique ID of contact at clicked list position using return from get_contact_details2
        String getDetailsString = "";
        try {
            getDetailsString= new RetrieveDetails2().execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        //create jsonobject from retrieveDetails returned string
        try{
            JSONObject contact = new JSONObject(getDetailsString);
            Log.d("jsonObj", contact.toString());
            uid = contact.getString("id");

        } catch (JSONException e){
            e.printStackTrace();
        }
        int toreturn = Integer.parseInt(uid);
        return toreturn;
    }
    private void createLogEntry(){
        //add LOG to contactlog table
        try {
            String logString = new logToDB().execute().get();
        } catch (InterruptedException e) {
            Toast.makeText(MainActivity.this, "No Connection, check database status", Toast.LENGTH_SHORT);
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    //creates url connection and retrieves json response as string
    private class RetrieveTask extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params){
            //chooses which script to query
            String strURL = "http://31.51.178.61:8080/KellysDB/get_all_contacts.php";
            URL url;
            StringBuffer sb = new StringBuffer();
            try{
                url = new URL(strURL);
                //creates and opens URL connection
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                //sets connection timeout to 5 seconds
                connection.setConnectTimeout(5000);
                connection.connect();
                //sets up InputStream to retrieve data from connection
                InputStream is = connection.getInputStream();
                //sets up bufferedreader to parse InputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                //creates variable and stores bufferedreader data in the variable line by line, then appends to StringBuffer
                String line;
                while ((line = br.readLine()) !=null){
                    sb.append(line + "\n");
                }
                br.close();
                is.close();
                connection.disconnect();
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
            //converts stringbuffer toString to be returned
            jsonLine = sb.toString();
            //if nothing returned error, return erorr
            if (jsonLine.equalsIgnoreCase("")){
                jsonLine = "Error";
            }
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            //creates pop up
            super.onPostExecute(result);
            Log.d("All Contacts:", result);
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Contacts Loaded", duration);
            toast.show();
        }
    }
    //modified retrievetask uses HTTP GET to pass ID to php script and return the response as a string
    private class RetrieveDetails extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){
            int passedID = toPass;

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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
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

    //modified retrievetask uses HTTP GET to pass ID to php script and return the response as a string
    private class RetrieveDetails2 extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params){

            //choosing which php script to access
            //encoding name of contact in UTF-8 to allow it to be understood by HTTP GET
            String append = "";
            try {
                 append = URLEncoder.encode(nameToPass, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String strURL = "http://31.51.178.61:8080/KellysDB/get_contact_details2.php?name="+append;
            URL url;
            StringBuffer sb = new StringBuffer();
            try {
                //converting String to URL
                url = new URL(strURL);
                //create HttpURLConnection and open
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //set connection timeout to 5 seconds
                connection.setConnectTimeout(5000);
                //create bufferedreader and inputstreamreader to parse data from the php script
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                //read json response line by line
                while ((line = br.readLine()) != null) {
                    //store each line in stringBuffer
                    sb.append(line + "\n");
                }
                br.close();
                connection.disconnect();
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
                    }
                });
            }  catch (IOException e){
                e.printStackTrace();
            }
            //if nothing returned error, return error
            if (jsonLine.equalsIgnoreCase("")){
                jsonLine = "Error";
            }
            //convert stringbuffer to string
            jsonLine = sb.toString();
            return jsonLine;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("some tag", result);

        }
    }
    //creates GET string for Log
    private String appendStringLog (){
        StringBuilder getAppend = new StringBuilder("?");
        getAppend.append("contactID=");
        getAppend.append(forLog);
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Timed Out! Check Database Status", Toast.LENGTH_SHORT).show();
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

