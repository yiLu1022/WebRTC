package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netatmo.auth.AuthManager;
import com.netatmo.dispatch.android.Dispatch;
import com.netatmo.gcm.GCMRegistration;
import com.netatmo.logger.Logger;
import com.netatmo.mbidon.pocwebrtc.data.RtcMessage;
import com.netatmo.mbidon.pocwebrtc.helper.RtcDataHelper;
import com.netatmo.notification.PushDispatcher;
import com.netatmo.notification.PushError;
import com.netatmo.notification.PushHandler;
import com.netatmo.notification.PushRegistration;
import com.netatmo.notification.RegistrationListener;
import com.netatmo.websocket.WebSocketManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Capturer;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
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

    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private SDPObserver sdpObserver;
    private MediaConstraints mediaConstraints;

    private RtcMessage rtcMessage;
    private SessionDescription remoteSDP;
    private SessionDescription localSDP;

    private AudioSource audioSource;
    private VideoSource videoSource;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;

    private VideoTrack remoteVideoTrack;
    private SurfaceViewRenderer videoRenderer;
    private SurfaceViewRenderer localRenderer;

    private EglBase rootEglBase = EglBase.create();

    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private JSONObject jsonRTC;

    private boolean isAlreadyHandlingOffer = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidInjection.inject(this);

        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(this, MANDATORY_PERMISSIONS, 1);
        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {

                Logger.i("Permission " + permission + " was not granted.");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                sendTestPush();
            }
        });

        videoRenderer = findViewById(R.id.fullscreen_video_view);
        localRenderer = findViewById(R.id.fullscreen_video_local);

        button = (Button) findViewById(R.id.disconnect);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                disconnect();
            }
        });

        videoRenderer.init(
                rootEglBase.getEglBaseContext(),
                null
        );
        videoRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        videoRenderer.setZOrderMediaOverlay(true);


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
                                try {
                                    jsonRTC = new JSONObject(data.getString("extra_params"));
                                    rtcMessage = RtcDataHelper.parseRtcMessage(jsonRTC.getString("data"));
                                    onRtcMessageReceived(rtcMessage);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                break;
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

        if (rtcMessage.sdp != null && !isAlreadyHandlingOffer) {
            isAlreadyHandlingOffer = true;
            handleSDP(rtcMessage);
        } else if (rtcMessage.candidate != null) {
            handleCandidate(rtcMessage);
        }
    }

    private void handleCandidate(RtcMessage rtcMessage) {
        Logger.i("Candidate : " + rtcMessage.candidate);
        peerConnection.addIceCandidate(new IceCandidate(
                "",
                rtcMessage.candidate.index,
                rtcMessage.candidate.candidate

        ));
    }



    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        videoSource = factory.createVideoSource(capturer);
        capturer.startCapture(800, 448, 30);

        localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addSink(localRenderer);
//        localVideoTrack.addRenderer(new VideoRenderer(localRenderer));
        return localVideoTrack;
    }

    private void handleSDP(RtcMessage rtcMessage) {
        remoteSDP = new SessionDescription(
                SessionDescription.Type.OFFER,
                rtcMessage.sdp.body
        );

        sdpObserver = new SDPObserver();
        mediaConstraints = new MediaConstraints();

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new SoftwareVideoEncoderFactory();
        decoderFactory = new SoftwareVideoDecoderFactory();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .createInitializationOptions());

        factory = new PeerConnectionFactory(options, encoderFactory, decoderFactory);

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(new ArrayList<>());
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        peerConnection = factory.createPeerConnection(rtcConfig, new PCObserver());

        Logger.i("SET REMOTE : " + remoteSDP);

        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");

        mediaStream.addTrack(createAudioTrack());
        VideoCapturer videoCapturer = createVideoCapturer();
        localVideoTrack = createVideoTrack(videoCapturer);
        peerConnection.addTrack(localVideoTrack)  ;
        peerConnection.addStream(mediaStream);
//        isAlreadyHandlingOffer = false;

        peerConnection.setRemoteDescription(sdpObserver, remoteSDP);
    }

    private AudioTrack createAudioTrack() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
        localAudioTrack.setEnabled(true);
        return localAudioTrack;
    }

    private VideoCapturer createVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(this);
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            Logger.d("Creating DEVICE: " + deviceName);
            if (enumerator.isFrontFacing(deviceName)) {
                Logger.d("Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    @Override
                    public void onCameraError(String s) {
                        Logger.i("WOLOLO onCameraError : " + s);
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Logger.i("WOLOLO onCameraDisconnected");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Logger.i("WOLOLO onCameraFreezed : " + s);
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Logger.i("WOLOLO onCameraOpening : " + s);
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Logger.i("WOLOLO onFirstFrameAvailable");
                    }

                    @Override
                    public void onCameraClosed() {
                        Logger.i("WOLOLO onCameraClosed");
                    }
                });
                Logger.i("WOLOLO onVideoCapturerCreated : " + videoCapturer);

                if (videoCapturer != null) {
                    Logger.i("WOLOLO onVideoCapturerCreated 2 : " + videoCapturer);
                    return videoCapturer;
                }
            }
        }
        Logger.e("NOVIDEOCREATED");
        return null;
    }


    public void sendAnswerSdp(final SessionDescription sdp) {
        if (sdp != null) {
            Dispatch.Background.async(() -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "Answer");
                    JSONObject sdpJson = new JSONObject();
                    sdpJson.put("type", "Live");
                    sdpJson.put("video_id", rtcMessage.sdp.videoId);
                    sdpJson.put("body", sdp.description);

                    json.put("sdp", sdpJson);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonRTC.put("action", "RTC");
                    jsonRTC.put("data", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Logger.i("SENDING SDP: " + jsonRTC.toString());
                webSocketManager.send(jsonRTC.toString());
            });
        }
    }

    public void sendCandidate(final IceCandidate iceCandidate) {
        if (iceCandidate != null) {
            Dispatch.Background.async(() -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "Candidate");
                    JSONObject candidateJson = new JSONObject();
                    candidateJson.put("sdp_m_line_index", iceCandidate.sdpMLineIndex);
                    candidateJson.put("candidate", iceCandidate.sdp);
                    json.put("candidate", candidateJson);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonRTC.put("action", "RTC");
                    jsonRTC.put("data", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Logger.i("SENDING CANDIDATE: " + jsonRTC.toString());
                webSocketManager.send(jsonRTC.toString());
            });
        }
    }

    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Logger.i("PIZZA : onSignalingChange : " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Logger.i("PIZZA : onIceConnectionChange : " + iceConnectionState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Logger.i("PIZZA : onIceConnectionReceivingChange : " + b);

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Logger.i("PIZZA : onIceGatheringChange : " + iceGatheringState);

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Logger.i("PIZZA : onIceCandidate : " + iceCandidate);
            sendCandidate(iceCandidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Logger.i("PIZZA : onIceCandidatesRemoved : " + iceCandidates);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Logger.i("PIZZA : onAddStream : " + mediaStream);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Logger.i("PIZZA : onRemoveStream : " + mediaStream);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Logger.i("PIZZA : onDataChannel : " + dataChannel);
        }

        @Override
        public void onRenegotiationNeeded() {
            Logger.i("PIZZA : onRenegotiationNeeded");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            for (MediaStream mediaStream : mediaStreams) {
                for (VideoTrack videoTrack : mediaStream.videoTracks) {
                    remoteVideoTrack = videoTrack;
                    remoteVideoTrack.addRenderer(new VideoRenderer(videoRenderer));
                    remoteVideoTrack.setEnabled(true);
                    break;
                }
            }
        }
    }

    private class SDPObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Logger.i("PIZZA : onCreateSuccess : " + sessionDescription);

            localSDP = new SessionDescription(sessionDescription.type, sessionDescription.description);
            peerConnection.setLocalDescription(sdpObserver, localSDP);
            sendAnswerSdp(localSDP);
        }

        @Override
        public void onSetSuccess() {
            Logger.i("PIZZA : onSetSuccess");
            peerConnection.createAnswer(sdpObserver, mediaConstraints);

        }

        @Override
        public void onCreateFailure(String s) {
            Logger.i("PIZZA : onCreateFailure: " + s);
        }

        @Override
        public void onSetFailure(String s) {
            Logger.i("PIZZA : onSetFailure : " + s);
        }
    }
}
