package com.netatmo.mbidon.pocwebrtc;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.netatmo.notification.NotificationHandler;
import com.netatmo.notification.NotificationHelper;
import com.netatmo.notification.NotificationViewConfig;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Application application;

    public AppModule(final Application application) {
        this.application = application;
    }

    @Singleton
    @Provides
    Application application() {
        return application;
    }


    @Singleton
    @Provides
    @NonNull
    List<NotificationHandler> provideNotificationHandlers() {
        ArrayList<NotificationHandler> handlers = new ArrayList<>();
        return handlers;
    }

    @Singleton
    @Provides
    @NonNull
    NotificationViewConfig provideNotificationViewConfig() {
        return new ApplicationNotificationViewConfig();
    }

}
