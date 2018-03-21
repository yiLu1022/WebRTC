package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.netatmo.api.error.RequestError;
import com.netatmo.auth.AuthManager;
import com.netatmo.auth.Listener;
import com.netatmo.auth.oauth.AuthScope;
import com.netatmo.logger.Logger;
import com.netatmo.notification.PushError;
import com.netatmo.notification.PushRegistration;
import com.netatmo.notification.RegistrationListener;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class LoginActivity extends AppCompatActivity {

    @Inject
    protected AuthManager authManager;

    @Inject
    protected PushRegistration pushRegistration;

    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        if (authManager.isLogged()) {
            registerNotifications();
            return;
        }

        setContentView(R.layout.activity_login);

        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        final String email = ((EditText) findViewById(R.id.email)).getText().toString();
        if (email.length() == 0) {
            return;
        }

        final String password = ((EditText) findViewById(R.id.password)).getText().toString();
        if (password.length() == 0) {
            return;
        }

        authManager.login(email, password, EnumSet.of(AuthScope.AccessCamera), new Listener() {
            @Override
            public void onSuccess() {
                registerNotifications();
            }

            @Override
            public boolean onFailure(@NonNull final RequestError error, final boolean alreadyHandled) {
                return false;
            }
        });
    }

    private void registerNotifications() {

        Logger.i("WOLOLO Registration start");
        pushRegistration.register(new RegistrationListener() {
            @Override
            public void onRegistrationFinished(@Nullable final List<PushError> errors) {
                Logger.i("WOLOLO Registration done : errors = " + errors);
                if (errors == null || errors.isEmpty()) {
                    launchPushActivity();
                }
            }
        });
    }

    private void launchPushActivity() {
        MainActivity.startActivity(this);
        finish();
    }
}
