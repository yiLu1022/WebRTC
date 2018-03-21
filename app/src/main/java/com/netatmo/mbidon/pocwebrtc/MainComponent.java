package com.netatmo.mbidon.pocwebrtc;

import com.netatmo.gcm.GCMModule;
import com.netatmo.notification.PushModule;
import com.netatmo.websocket.WebSocketModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        PushModule.class,
        GCMModule.class,
        WebSocketModule.class,
        GCMDependenciesModule.class,
        GCMAppModule.class,
})
public interface MainComponent {
    void inject(MainApplication mainApplication);
}
