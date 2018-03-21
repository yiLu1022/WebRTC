package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netatmo.auth.AuthManager;
import com.netatmo.dispatch.android.Dispatch;
import com.netatmo.gcm.GCMRegistration;
import com.netatmo.logger.Logger;
import com.netatmo.mbidon.pocwebrtc.data.RtcMessage;
import com.netatmo.mbidon.pocwebrtc.data.Sdp;
import com.netatmo.mbidon.pocwebrtc.helper.RtcDataHelper;
import com.netatmo.notification.PushDispatcher;
import com.netatmo.notification.PushError;
import com.netatmo.notification.PushHandler;
import com.netatmo.notification.PushRegistration;
import com.netatmo.notification.RegistrationListener;
import com.netatmo.websocket.WebSocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class MainActivity extends AppCompatActivity {
    @Inject
    GCMRegistration gcmRegistration;

    @Inject
    PushRegistration pushRegistration;

    @Inject
    PushDispatcher pushDispatcher;

    @Inject
    AuthManager authManager;

    @Inject
    WebSocketManager webSocketManager;

    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private boolean isHandlingRtc = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidInjection.inject(this);

        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                sendTestPush();
            }
        });

        button = (Button) findViewById(R.id.disconnect);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                disconnect();
            }
        });

        pushDispatcher.setDefaultHandler(new PushHandler() {
            @Override
            public void handlePush(@Nullable final String pushType, @NonNull final Bundle data) {
                Dispatch.Main.async(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.notification)).setText(data.toString());
                        Logger.i(data.getString("alert"));
                        Logger.i(data.getString("extra_params"));
                        Logger.i("WOLO : " + pushType);
                        switch (pushType) {
                            case "rtc_message":
                                if (!isHandlingRtc) {
                                    isHandlingRtc = true;
                                    try {
                                        JSONObject obj = new JSONObject(data.getString("extra_params"));
                                        RtcMessage rtcMessage = RtcDataHelper.parseRtcMessage(obj.getString("data"));
                                        onRtcMessageReceived(rtcMessage);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                }
                        }
                    }
                });
            }
        });
    }


    private void disconnect() {

        pushRegistration.unregister(new RegistrationListener() {
            @Override
            public void onRegistrationFinished(@Nullable final List<PushError> errors) {
                authManager.logout();
                Dispatch.Main.async(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.startActivity(MainActivity.this);
                    }
                });
            }
        });
    }

    private void sendTestPush() {
        RtcMessage rtcMessage = RtcDataHelper.parseRtcMessage("{\n" +
                "            \"type\": \"Offer\",\n" +
                "            \"sdp\": {\n" +
                "                \"type\": \"Call\",\n" +
                "                \"video_id\": \"1234567890\",\n" +
                "                \"body\": \"v=0\\r\\no=- 4811089042516182160 0 IN IP4 0.0.0.0\\r\\ns=-\\r\\nt=0 0\\r\\na=ice-options:trickle\\r\\nm=video 9 UDP/TLS/RTP/SAVPF 96\\r\\nc=IN IP4 0.0.0.0\\r\\na=setup:actpass\\r\\na=ice-ufrag:qZEm4I5yeaRV5txKD2AiJLN7KRfUdlSh\\r\\na=ice-pwd:dYPBUEb+5f11g/x/kpVldejHdHlxAkJn\\r\\na=sendrecv\\r\\na=rtcp-mux\\r\\na=rtcp-rsize\\r\\na=rtpmap:96 VP8/90000\\r\\na=rtcp-fb:96 nack\\r\\na=rtcp-fb:96 nack pli\\r\\na=mid:video0\\r\\na=fingerprint:sha-256 75:66:9A:7C:65:14:62:04:1E:AA:56:94:AF:65:D4:80:18:90:EA:A8:EE:94:57:E2:B9:19:70:F6:4D:F3:4F:2D\\r\\nm=audio 9 UDP/TLS/RTP/SAVPF 97\\r\\nc=IN IP4 0.0.0.0\\r\\na=setup:actpass\\r\\na=ice-ufrag:Qo0hsS5OVYmUf9b5eizCR3nuQeXFPOmE\\r\\na=ice-pwd:QIqqJzicpm84OdZHbo7byX6Uq9m+vmCJ\\r\\na=sendrecv\\r\\na=rtcp-mux\\r\\na=rtcp-rsize\\r\\na=rtpmap:97 OPUS/48000/2\\r\\na=rtcp-fb:97 nack\\r\\na=rtcp-fb:97 nack pli\\r\\na=mid:audio1\\r\\na=fingerprint:sha-256 75:66:9A:7C:65:14:62:04:1E:AA:56:94:AF:65:D4:80:18:90:EA:A8:EE:94:57:E2:B9:19:70:F6:4D:F3:4F:2D\\r\\n\"\n" +
                "            }\n" +
                "        }");
        onRtcMessageReceived(rtcMessage);
    }


    // WEBRTC


    private void onRtcMessageReceived(RtcMessage rtcMessage) {
        Logger.i("rtcMessage: " + rtcMessage.toString());

//        peerConnectionClient.setRemoteDescription(params.offerSdp);
//        peerConnectionClient.createAnswer();


        isHandlingRtc = false;
    }
}
