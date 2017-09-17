package me.edgeconsult.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

    private static class ViewHolder{
        TextView username;
        TextView time;
        TextView body;
    }

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    private ListView MessagesWrapper;
    private EditText Input;
    private Button SendButton;

    private OkHttpClient client;

    private Message[] messages = {
            new Message("헬로 키티", 44883L, "Welcome to Android Chat!"),
            new Message("헬로 키티", 44883L, "서울치킨최고"),
            new Message("헬로 키티", 44883L, "Apple"),
            new Message("헬로 키티", 44883L, "사과"),
            new Message("헬로 키티", 44883L, "배"),
            new Message("헬로 키티", 44883L, "Orange"),
            new Message("헬로 키티", 44883L, "Banana"),
            new Message("헬로 키티", 44883L, "초코바나나"),
            new Message("헬로 키티", 44883L, "Pineapple"),
            new Message("헬로 키티", 44883L, "Lychee"),
            new Message("헬로 키티", 44883L, "바나나우유"),
            new Message("헬로 키티", 44883L, "Blueberry"),
            new Message("헬로 키티", 44883L, "Lime"),
            new Message("헬로 키티", 44883L, "Mango"),
            new Message("헬로 키티", 44883L, "Strawberry"),
            new Message("헬로 키티", 44883L, "Pomelo"),
            new Message("헬로 키티", 44883L, "Grapefruit"),
            new Message("헬로 키티", 44883L, "Peach"),
            new Message("헬로 키티", 44883L, "Pear")
    };

    private ArrayList<Message> messagesList;
    private ArrayAdapter<Message> messagesAdapter;

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

        messagesList = new ArrayList<>(Arrays.asList(messages));

        messagesAdapter =
                new ArrayAdapter<Message>(this,
                        R.layout.messages_list_item,
                        messagesList) {

            @NonNull
            public View getView(int position,
                                View convertView,
                                @NonNull ViewGroup parent) {
                Message currentMessage = messagesList.get(position); // !!!!! 버그　여기　있네！ !!!!!!!
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
                time.setText(String.valueOf(currentMessage.getTime()));
                body.setText(currentMessage.getBody());

                return convertView;
            }
        };

        MessagesWrapper = (ListView) findViewById(R.id.messages_wrapper);
        Input = (EditText) findViewById(R.id.input);
        SendButton = (Button) findViewById(R.id.send_button);

        MessagesWrapper.setAdapter(messagesAdapter);

        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messagesList.add(new Message("윤안톤", 54664545L, Input.getText().toString()));
                messagesAdapter.notifyDataSetChanged();
                Input.setText("");
            }
        });

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
