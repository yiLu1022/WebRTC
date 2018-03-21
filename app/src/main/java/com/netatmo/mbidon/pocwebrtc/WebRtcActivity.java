package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.netatmo.logger.Logger;
import com.netatmo.mbidon.pocwebrtc.data.Sdp;
import com.netatmo.websocket.base.WebSocket;
import com.netatmo.websocket.base.WebSocketConnection;
import com.netatmo.websocket.base.WebSocketException;

/**
 * Created by mbidon on 21/03/18.
 */
public class WebRtcActivity extends AppCompatActivity {

    public static void startActivity(@NonNull Context context, Sdp sdp) {
        Intent intent = new Intent(context, WebRtcActivity.class);
        intent.putExtra("sdp", sdp);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Sdp sdp = getIntent().getExtras().getParcelable("sdp");

        setContentView(R.layout.activity_webrtc);

        Logger.i("Starting webrtc with " + sdp);

        openWebsocket();
    }

    private void openWebsocket() {
        WebSocketConnection webSocketConnection = new WebSocketConnection();
        try {
            webSocketConnection.connect(
                    "wss://my.inte.netatmo.com/ws/",
                    new WebSocket.ConnectionHandler() {
                        @Override
                        public void onOpen() {
                            Logger.i("onOpen");
                        }

                        @Override
                        public void onClose(int code, String reason) {
                            Logger.i("onClose: " + reason);
                        }

                        @Override
                        public void onTextMessage(String payload) {
                            Logger.i("onTextMessage: " + payload);
                        }

                        @Override
                        public void onRawTextMessage(byte[] payload) {
                            Logger.i("onRawTextMessage: " + payload);
                        }

                        @Override
                        public void onBinaryMessage(byte[] payload) {
                            Logger.i("onBinaryMessage: " + payload);
                        }
                    }
            );
        } catch (WebSocketException e) {
            Logger.e(e);
        }
    }
}
