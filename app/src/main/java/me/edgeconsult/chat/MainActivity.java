package me.edgeconsult.chat;

import android.app.NotificationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

    private static class ViewHolder{
        TextView username;
        TextView time;
        TextView body;
    }

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    private ListView MessagesWrapper;
    private EditText Input;
    private Button SendButton;

    private NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

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
                time.setText(String.valueOf(DateFormat.format("HH:MM", new Date(currentMessage.getTime())).toString()));
                body.setText(currentMessage.getBody());

                return convertView;
            }
        };

        // messagesAdapter.setNotifyOnChange(true);

        MessagesWrapper = (ListView) findViewById(R.id.messages_wrapper);
        Input = (EditText) findViewById(R.id.input);
        SendButton = (Button) findViewById(R.id.send_button);

        MessagesWrapper.setAdapter(messagesAdapter);

        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ed_text = Input.getText().toString().trim();
                if (ed_text.isEmpty() || ed_text.length() == 0 || ed_text.equals("")) {
                    //EditText is empty
                } else {
                    String msg = "{ \"type\": \"message\", \"data\": { \"messageBody\": \"" + ed_text + "\" } }";
                    ws.send(msg);
                    Input.setText("");
                }
            }
        });

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.user);
        mBuilder.setContentTitle("Notification Alert, Click Me!");
        mBuilder.setContentText("Hi, This is Android Notification Detail!");

        mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);

        client = new OkHttpClient();

        Request request = new Request.Builder().url("wss://owncloudhk.net").build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                final String jString =
                        "{"
                                + " \"type\": \"login\","
                                + " \"data\": {"
                                + "             \"username\": \"ANDROID\","
                                + "             \"password\": \"logcat\""
                                + "           }"
                                + "}";
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
                                        mNotificationManager.notify(3, mBuilder.build());
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
                                    mNotificationManager.notify(1, mBuilder.build());
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
                                    mNotificationManager.notify(2, mBuilder.build());
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
                                    mNotificationManager.notify(3, mBuilder.build());
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

        client.dispatcher().executorService().shutdown();
    }
}
