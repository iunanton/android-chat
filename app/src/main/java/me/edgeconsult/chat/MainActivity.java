package me.edgeconsult.chat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity implements OnAccountsUpdateListener {
    private AccountManager accountManager;

    private String username = null;
    private String password = null;
    private int request_code = 1;

    private NotificationManager mNotificationManager;
    private static int notificationID = 1;
    private PendingIntent mPendingIntent;

    private Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private static class ViewHolder{
        TextView username;
        TextView time;
        TextView body;
    }

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    private ListView MessagesWrapper;
    private EditText Input;
    private ImageButton SendButton;

    // private NotificationCompat.Builder mBuilder;

    private OkHttpClient client;

    private WebSocket ws;

    private ArrayList<Message> messagesList;
    private ArrayAdapter<Message> messagesAdapter;

    private final class EchoWebSocketListener extends WebSocketListener {


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences credentials = this.getPreferences(Context.MODE_PRIVATE);
        // username = credentials.getString(getString(R.string.saved_username), null);
        // password = credentials.getString(getString(R.string.saved_password), null);
        accountManager = AccountManager.get(getApplicationContext());
        if (accountManager.getAccounts().length > 0) {
            // we save only 1 account
            Account account = accountManager.getAccounts()[0];
            final String str = accountManager.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
            });
            /*Account[] account = am.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
            Log.i("AccountManager", am.toString());
            Log.i("AccountManager", am.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE).toString());
            for (int i=0; i<account.length; i++) {
                Log.i("AccountManager", account[i].toString());
                String authTokenType = "";
                final AccountManagerFuture<Bundle> future = am.getAuthToken(account[i], authTokenType, null, this, null,null);
                try {
                    Bundle bnd = future.getResult();
                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i("AccountManager", authtoken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (username == null || password == null) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, request_code);
        }
        messagesList = new ArrayList<>();

        messagesAdapter =
                new ArrayAdapter<Message>(this,
                        R.layout.messages_list_item,
                        messagesList) {

                    @NonNull
                    public View getView(int position,
                                        View convertView,
                                        @NonNull ViewGroup parent) {
                        Message currentMessage = messagesList.get(position);
                        if (convertView == null) {
                            convertView = getLayoutInflater()
                                    .inflate(R.layout.messages_list_item, parent, false);
                            ViewHolder viewHolder = new ViewHolder();
                            viewHolder.username = convertView.findViewById(R.id.message_username);
                            viewHolder.time = convertView.findViewById(R.id.message_time);
                            viewHolder.body = convertView.findViewById(R.id.message_body);
                            convertView.setTag(viewHolder);
                        }

                        TextView username =
                                ((ViewHolder) convertView.getTag()).username;
                        TextView time =
                                ((ViewHolder) convertView.getTag()).time;
                        TextView body =
                                ((ViewHolder) convertView.getTag()).body;

                        username.setText(currentMessage.getUsername());
                        Date date = new Date(currentMessage.getTime());
                        time.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date));
                        body.setText(currentMessage.getBody());

                        return convertView;
                    }
                };

        // messagesAdapter.setNotifyOnChange(true);

        MessagesWrapper = (ListView) findViewById(R.id.messages_wrapper);
        Input = (EditText) findViewById(R.id.input);
        SendButton = (ImageButton) findViewById(R.id.send_button);

        MessagesWrapper.setAdapter(messagesAdapter);

        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ed_text = Input.getText().toString().trim().replaceAll("\\r|\\n", " ");
                if (ed_text.isEmpty() || ed_text.length() == 0 || ed_text.equals("")) {
                    //EditText is empty
                } else {
                    String msg = "{ \"type\": \"message\", \"data\": { \"messageBody\": \"" + ed_text + "\" } }";
                    ws.send(msg);
                    Input.setText("");
                }
            }
        });

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mPendingIntent = PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

        client = new OkHttpClient();

        Request request = new Request.Builder().url("wss://owncloudhk.net").build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                final String jString = "{\"type\":\"login\",\"data\":{\"username\":\"" + username
                        + "\",\"password\":\"" + password + "\"}}";
                Log.i(MAIN_ACTIVITY_TAG, jString);
                webSocket.send(jString);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    final JSONObject message = new JSONObject(text);
                    final String type = message.getString("type");
                    JSONObject data = message.getJSONObject("data");
                    switch (type) {
                        case "context": {
                            final JSONArray users = data.getJSONArray("users");
                            final JSONArray messages = data.getJSONArray("messages");
                            for (int i = 0; i < messages.length(); ++i) {
                                JSONObject item = messages.getJSONObject(i);
                                final String username = item.getString("username");
                                final Long timestamp = item.getLong("timestamp");
                                final String messageBody = item.getString("messageBody");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messagesAdapter.add(new Message(username, timestamp, messageBody));
                                    }
                                });
                            }
                            break;
                        }
                        case "userJoined":
                            final String username = data.getString("username");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                                    mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                                    mBuilder.setColor(0xFF00CCCC);
                                    mBuilder.setLights(0xFF00CCCC, 500, 1500);
                                    mBuilder.setSound(uri);
                                    mBuilder.setContentTitle("New user joined");
                                    mBuilder.setContentText(username + " joined! Say \"Hi\" to him!");
                                    mBuilder.setContentIntent(mPendingIntent);
                                    mBuilder.setAutoCancel(true);
                                    mNotificationManager.notify(notificationID, mBuilder.build());
                                    Toast.makeText(getApplicationContext(),
                                            username + " joined",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case "userLeft":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                                    mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                                    mBuilder.setColor(0xFF00CCCC);
                                    mBuilder.setLights(0xFF00CCCC, 500, 1500);
                                    mBuilder.setSound(uri);
                                    mBuilder.setContentTitle("User left");
                                    mBuilder.setContentText("One user just left chat..");
                                    mBuilder.setContentIntent(mPendingIntent);
                                    mBuilder.setAutoCancel(true);
                                    mNotificationManager.notify(notificationID, mBuilder.build());
                                    Toast.makeText(getApplicationContext(),
                                            "user left",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case "messageAdd":
                            final String message_body = data.getString("messageBody");
                            final Long message_timestamp = data.getLong("timestamp");
                            final String message_username = data.getString("username");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                                    mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                                    mBuilder.setColor(0xFF00CCCC);
                                    mBuilder.setLights(0xFF00CCCC, 500, 1500);
                                    mBuilder.setSound(uri);
                                    mBuilder.setContentTitle("New message");
                                    mBuilder.setContentText(message_username + ": " + message_body);
                                    mBuilder.setContentIntent(mPendingIntent);
                                    mBuilder.setAutoCancel(true);
                                    mNotificationManager.notify(notificationID, mBuilder.build());
                                    messagesAdapter.add(new Message(message_username, message_timestamp, message_body));
                                }
                            });
                            break;
                        default:
                            break;
                    }
                } catch (final JSONException e) {
                    Log.e(MAIN_ACTIVITY_TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        accountManager.addOnAccountsUpdatedListener(this, null, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountManager.removeOnAccountsUpdatedListener(this);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (accounts.length > 0) {
            return;
        }
        Intent intent = new Intent(this, AuthenticatorActivity.class);
        startActivity(intent);
        finish();
    }

}
