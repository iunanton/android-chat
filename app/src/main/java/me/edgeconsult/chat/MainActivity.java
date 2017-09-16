package me.edgeconsult.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    private TextView MessagesWrapper;

    private OkHttpClient client;

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
                final String type = message.getString("type");
                output(type);
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

        MessagesWrapper = (TextView) findViewById(R.id.messages_wrapper);

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
                MessagesWrapper.setText(txt);
            }
        });
    }
}
