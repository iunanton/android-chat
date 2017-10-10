package me.edgeconsult.chat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by yun on 10/11/17.
 */

public class MyClient {
    private static OkHttpClient client;
    private static WebSocket ws;
    private static Request request;
    private static WebSocketListener listener;

    public static WebSocket getWebSocket(Request r, WebSocketListener l) {
        if (client == null) {
            request = r;
            listener = l;
            client = new OkHttpClient();
            ws = client.newWebSocket(request, listener);
            client.dispatcher().executorService().shutdown();
        }
        return ws;
    }

    public static void closeWebSocket() {
        if (client != null) {
            ws.close(1000, "closing");
            client = null;
        }
    }
}
