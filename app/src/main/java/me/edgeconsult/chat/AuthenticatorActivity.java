package me.edgeconsult.chat;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private static final String AUTHENTICATOR_ACTIVITY_TAG = AuthenticatorActivity.class.getSimpleName();
    //public static final String AUTH_TOKEN_TYPE = "Full access";

    private static String TAG = "AuthenticatorActivity";
    private TextInputLayout usernameInputLayout;
    private String username;
    private TextInputLayout passwordInputLayout;
    private String password;
    private ProgressBar progressBar;
    private Button btn_login;

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        mAccountManager = AccountManager.get(getBaseContext());
        usernameInputLayout = findViewById(R.id.username_layout);
        passwordInputLayout = findViewById(R.id.password_layout);
        progressBar = findViewById(R.id.progressBar);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        // Log.d(TAG, "Invoked login function");



        btn_login.setEnabled(false);
        usernameInputLayout.setEnabled(false);
        passwordInputLayout.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        username = ((EditText) findViewById(R.id.input_username)).getText().toString();
        password = ((EditText) findViewById(R.id.input_password)).getText().toString();

        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... voids) {
                String urlParameters;
                String targetURL = "https://owncloudhk.net/oauth";
                URL url;
                HttpsURLConnection connection = null;
                try {
                    urlParameters = "grant_type=password&username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
                    url = new URL(targetURL);
                    connection = (HttpsURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:55.0) Gecko/20100101 Firefox/55.0");
                    connection.setRequestProperty("Accept", "*/*");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    JSONObject json = new JSONObject(response.toString());
                    if (json.has("access_token")) {
                        String authtoken = json.getString("access_token");
                        final Intent res = new Intent();
                        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.account_type));
                        res.putExtra(AccountManager.KEY_AUTHTOKEN, authtoken);
                        return res;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if(connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(Intent intent) {
                finishLogin(intent);
            }
        }.execute();
    }

    private void finishLogin(Intent intent) {
        progressBar.setVisibility(View.INVISIBLE);
        usernameInputLayout.setEnabled(true);
        passwordInputLayout.setEnabled(true);
        btn_login.setEnabled(true);
        if (intent != null) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = getString(R.string.auth_token_type);
            mAccountManager.addAccountExplicitly(account, null, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(this, "Username or password invalid", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String username = ((EditText) findViewById(R.id.input_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.input_password)).getText().toString();

        if (username.isEmpty() || username.length() < 4) {
            ((EditText) findViewById(R.id.input_username)).setError("more than 4 alphanumeric characters");
            valid = false;
        } else {
            ((EditText) findViewById(R.id.input_username)).setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            ((EditText) findViewById(R.id.input_password)).setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            ((EditText) findViewById(R.id.input_password)).setError(null);
        }

        return valid;
    }
}
