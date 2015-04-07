package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class LoginActivity extends ActionBarActivity {

    public static final String APP_TAG = "PlateRateApp";
    enum LoginState {LOGIN_STATE_STARTED, LOGIN_STATE_DONE};

    ProgressBar pb;
    Button actionFbLoginButton;
    Button actionTwitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
    }

    private void setupViews() {
        // Set up the submit button click handler
        actionFbLoginButton = (Button) findViewById(R.id.action_fb_login_button);
        actionFbLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onFacebookLoginButtonClicked();
            }
        });

        actionTwitterLoginButton = (Button) findViewById(R.id.action_tw_login_button);
        actionTwitterLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onTwitterLoginButtonClicked();
            }
        });

        // get progress bar
        pb = (ProgressBar) findViewById(R.id.progressBarLogin);
        FoodAppUtils.assignProgressBarStyle(this, pb);

        changeLoginState(LoginState.LOGIN_STATE_DONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Done with authentication, show Authenticated Activity now
    private void finishActivity() {

        changeLoginState(LoginState.LOGIN_STATE_DONE);

        // Start an intent for the dispatch activity
        Intent intent = new Intent(LoginActivity.this, DispatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // For facebook login
    private void onFacebookLoginButtonClicked() {
        changeLoginState(LoginState.LOGIN_STATE_STARTED);
        List<String> permissions = Arrays.asList("public_profile");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user != null) {
                    if (user.isNew()) {
                        // Get username and save it
                        saveFacebookUserData();
                    } else {
                        // We are done, show authenticated activity
                        finishActivity();
                    }
                }
                else {
                    Log.e("FB login error", "user is null");
                    changeLoginState(LoginState.LOGIN_STATE_DONE);
                    Toast.makeText(getApplicationContext(), "Facebook login error: User is null", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    // Get user data from Facebook and save it.
    private void saveFacebookUserData() {
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(
                    ParseFacebookUtils.getSession(),
                    new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user,
                                                Response response) {
                            if (user != null) {
                                saveAppUserName(user.getFirstName()+" "+user.getLastName());
                                // We are done, show authenticated activity
                                finishActivity();
                            } else if (response.getError() != null) {
                                if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                        || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.session_invalid_error,
                                            Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.login_generic_error,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
            request.executeAsync();

        }
    }

    // For Twitter login
    private void onTwitterLoginButtonClicked() {
        changeLoginState(LoginState.LOGIN_STATE_STARTED);
        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d(APP_TAG, "Uh oh. The user cancelled the Twitter login.");
                    changeLoginState(LoginState.LOGIN_STATE_DONE);
                } else if (user.isNew()) {
                    Log.d(APP_TAG, "User signed up and logged in through Twitter!");
                    // get username and save it
                    saveTwitterUserData();
                } else {
                    Log.d(APP_TAG, "User logged in through Twitter!");
                    // We are done, show authenticated activity
                    finishActivity();
                }
            }

        });
    }

    // Get user data from Twitter and save it.
    private void saveTwitterUserData() {
        AsyncTask t = new AsyncTask() {
            @Override
            protected Boolean doInBackground(Object[] params) {
                String userName = "unknown";
                HttpClient client = new DefaultHttpClient();
                HttpGet verifyGet = new HttpGet(
                        "https://api.twitter.com/1.1/account/verify_credentials.json");
                ParseTwitterUtils.getTwitter().signRequest(verifyGet);
                try {
                    HttpResponse response = client.execute(verifyGet);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        HttpEntity entity = response.getEntity();
                        String data = EntityUtils.toString(entity);
                        try {
                            JSONObject responseJSON = new JSONObject(data);
                            userName = responseJSON.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    // Save and we are done.
                    saveAppUserName(userName);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            protected void onPostExecute(Object o) {
                // We are done, show authenticated activity
                finishActivity();
            }
        };
        t.execute();
    }

    private void changeLoginState(LoginState loginState) {
        switch (loginState) {
            case LOGIN_STATE_STARTED:
                pb.setVisibility(ProgressBar.VISIBLE);
                actionTwitterLoginButton.setEnabled(false);
                actionFbLoginButton.setEnabled(false);
                break;
            case LOGIN_STATE_DONE:
                pb.setVisibility(ProgressBar.GONE);
                actionTwitterLoginButton.setEnabled(true);
                actionFbLoginButton.setEnabled(true);
                break;
        }
    }

    private void saveAppUserName(String userName) {
        ParseUser.getCurrentUser().put("appUserName", userName);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i(Constants.TAG, e==null?"Login Success":e.getMessage());
            }
        });
    }
}
