package me.edgeconsult.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

    private static class Message {
        String username;
        Integer time;
        String body;

        Message(String username, Integer time, String body) {
            this.username = username;
            this.time = time;
            this.body = body;
        }
    }

    private static class ViewHolder{
        TextView username;
        TextView time;
        TextView body;
    }

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    // private TextView MessagesWrapper;

    private ListView MessagesWrapper;

    private ArrayAdapter<Message> messagesAdapter;

    private OkHttpClient client;

    private Message[] messages = {
            new Message("Hellokitty", 44883, "Welcome to Android Chat!"),
            new Message("Hellokitty", 44883, "서울치킨최고"),
            new Message("Hellokitty", 44883, "Apple"),
            new Message("Hellokitty", 44883, "사과"),
            new Message("Hellokitty", 44883, "배"),
            new Message("Hellokitty", 44883, "Orange"),
            new Message("Hellokitty", 44883, "Banana"),
            new Message("Hellokitty", 44883, "초코바나나"),
            new Message("Hellokitty", 44883, "Pineapple"),
            new Message("Hellokitty", 44883, "Lychee"),
            new Message("Hellokitty", 44883, "바나나우유"),
            new Message("Hellokitty", 44883, "Blueberry"),
            new Message("Hellokitty", 44883, "Lime"),
            new Message("Hellokitty", 44883, "Mango"),
            new Message("Hellokitty", 44883, "Strawberry"),
            new Message("Hellokitty", 44883, "Pomelo"),
            new Message("Hellokitty", 44883, "Grapefruit"),
            new Message("Hellokitty", 44883, "Peach"),
            new Message("Hellokitty", 44883, "Pear")
    };

    ArrayList<Message> Target;

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        private final static String jString =
                "{"
                        + " \"type\": \"login\","
                        + " \"data\": {"
                        + "             \"username\": \"ANDROID\","
                        + "             \"password\": \"logcat\""
                        + "           }"
                        + "}";

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i(MAIN_ACTIVITY_TAG, jString);
            webSocket.send(jString);
            }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output(text);
            try {
                JSONObject message = new JSONObject(text);
                String type = message.getString("type");
                JSONObject data = message.getJSONObject("data");
                switch (type) {
                    case "context":
                        output(type);
                        break;
                    case "userJoined":
                        final String username = data.getString("username");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                Toast.makeText(getApplicationContext(),
                                        "user left",
                                        Toast.LENGTH_LONG).show();
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Target = new ArrayList<>(Arrays.asList(messages));

        messagesAdapter =
                new ArrayAdapter<Message>(this,
                        R.layout.messages_list_item,
                        Target) {
            @NonNull
            public View getView(int position,
                                View convertView,
                                @NonNull ViewGroup parent) {
                Message currentMessage = messages[position];
                // Inflate only once
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

                username.setText(currentMessage.username);
                time.setText(String.valueOf(currentMessage.time));
                body.setText(currentMessage.body);

                return convertView;
            }
        };

        //messagesAdapter.setNotifyOnChange(true);

        MessagesWrapper = (ListView) findViewById(R.id.messages_wrapper);
        MessagesWrapper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long rowId) {

                // Generate a message based on the position
                final String message = "You clicked on " + messages[position].body;

                // Use the message to create a Toast
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        MessagesWrapper.setAdapter(messagesAdapter);
        Target.add(new Message("iunanton", 5466, "Hello"));
        messagesAdapter.notifyDataSetChanged();
        client = new OkHttpClient();

        Request request = new Request.Builder().url("wss://owncloudhk.net").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //MessagesWrapper.setText(txt);
            }
        });
    }
}
