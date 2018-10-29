package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import dagger.android.AndroidInjection;

public class CallActivity extends AppCompatActivity {

    private SurfaceViewRenderer videoRenderer;
    private EglBase rootEglBase = EglBase.create();

    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CallActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AndroidInjection.inject(this);

        setContentView(R.layout.activity_call);

        videoRenderer = findViewById(R.id.fullscreen_video_call);

        videoRenderer.init(
                rootEglBase.getEglBaseContext(),
                null
        );
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
}
