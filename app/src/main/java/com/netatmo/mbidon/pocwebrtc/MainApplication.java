package com.netatmo.mbidon.pocwebrtc;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.netatmo.logger.Logger;
import com.netatmo.logger.android.AndroidLog;
import com.netatmo.notification.NotificationHandler;
import com.netatmo.notification.PushDispatcher;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;

public class MainApplication extends Application implements
        HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    private static MainComponent appComponent;
    private static Context applicationContext;
    @Inject
    protected DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    protected DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject
    protected DispatchingAndroidInjector<BroadcastReceiver> dispatchingBroadcastReceiverInjector;
    @Inject
    protected List<NotificationHandler> notificationHandlers;
    @Inject
    protected PushDispatcher pushDispatcher;

    /**
     * Access to the application component.
     *
     * @return application component.
     */
    public static MainComponent component() {
        return appComponent;
    }

    public static Context get() {
        return applicationContext;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingBroadcastReceiverInjector;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        appComponent = initComponent();
        if (BuildConfig.DEBUG) {
            Logger.addConsumer(new AndroidLog());
        }

        for (NotificationHandler notificationHandler : notificationHandlers) {
            notificationHandler.registerTo(pushDispatcher);
        }
    }

    /**
     * Initialize the dagger graph.
     *
     * @return application component build form dagger graph.
     */
    protected MainComponent initComponent() {
        MainComponent component = DaggerMainComponent
                .builder()
                .appModule(new AppModule(this))
                .build();
        component.inject(this);
        return component;
    }
}
