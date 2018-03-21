package com.netatmo.mbidon.pocwebrtc;

import android.support.annotation.NonNull;

import com.netatmo.notification.NotificationViewConfig;

/**
 * Used as an example to show how an app could influence the notification ui of notification
 * created by libraries.
 */
public class ApplicationNotificationViewConfig extends NotificationViewConfig {

    private static final boolean OVERRIDE = false;

    @Override
    public int defaultIcon(@NonNull String pushType) {
        return R.mipmap.ic_launcher;
    }

    @Override
    public int defaultColor(@NonNull String pushType) {
        return R.color.colorPrimary;
    }

    @Override
    public boolean overrideHandlerValues(@NonNull String pushType) {
        return OVERRIDE;
    }
}
