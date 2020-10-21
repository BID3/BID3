package com.bignerdranch.android.pife11;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Throwable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import android.util.Log;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bFetch;
    private LoginButton loginButton;
    private TextView textView;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView mResult;
    public static CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Handles Transition to Create Account Page
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        /*
            Method #0: Allowing Edit Text Boxes to Clear upon clicking
         */
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextEmail.setOnClickListener(this);
        editTextPassword.setOnClickListener(this);

        /*
            Method #1: If Users Select the Option to Register for a New Account
        */
        textView = (TextView) findViewById(R.id.createAccount);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateAccount();
            }
        });


        /*
            Method #2: Users opt selecting the option to login via Facebook
         */

        loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login Success");
            }

            @Override
            public void onCancel() {
                textView.setText("Login Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                textView.setText("Error");
            }
        });

        /*
            Method #3: Fetching Data from MongoDB
         */
        bFetch = (Button) findViewById(R.id.fetch);
        bFetch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetch(v);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void openCreateAccount() {
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.email:
                editTextEmail.getText().clear();
                break;
            case R.id.password:
                editTextPassword.getText().clear();
        }
    }

    /*
        Get User Data from MongoDB Collections
     */
    public void fetch(View v){
        GetContactsAsyncTask task = new GetContactsAsyncTask();
        ArrayList<UserInfo> returnValues;

        try {
            returnValues = task.execute().get();
            UserInfo FetchedData = (UserInfo) returnValues.toArray()[0];

            editTextEmail.setText(FetchedData.getEmail());
            editTextPassword.setText(FetchedData.getPassword());

            Toast.makeText(this, "Fetched from MongoDB!!", Toast.LENGTH_SHORT).show();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class GetContactsAsyncTask extends AsyncTask<UserInfo, Void, ArrayList<UserInfo>> {
        String server_output = null;
        String temp_output = null;

        @Override
        protected ArrayList<UserInfo> doInBackground(UserInfo... arg0) {

            ArrayList<UserInfo> mycontacts = new ArrayList<UserInfo>();

            try {
                SupportData sd = new SupportData();
                URL url = new URL(sd.buildContactsFetchURL());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                while ((temp_output = br.readLine()) != null) {
                    server_output = temp_output;
                }

                String mongoarray = "{ DB_output: " +server_output+ "}";
                Object o = com.mongodb.util.JSON.parse(mongoarray);

                DBObject dbObj = (DBObject) o;
                BasicDBList contacts = (BasicDBList) dbObj.get("DB_output");
                for (Object obj : contacts) {
                    DBObject userObj = (DBObject) obj;

                    UserInfo temp = new UserInfo();
                    temp.setUsername(userObj.get("username").toString());
                    temp.setPassword(userObj.get("password").toString());
                    mycontacts.add(temp);
                }

            }catch (Exception e) {
                e.getMessage();
            }

            return mycontacts;
        }
    }

}




