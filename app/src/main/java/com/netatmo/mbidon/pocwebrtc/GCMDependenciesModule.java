package com.netatmo.mbidon.pocwebrtc;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.netatmo.api.AppType;
import com.netatmo.api.ApplicationParameters;
import com.netatmo.auth.AuthClient;
import com.netatmo.auth.AuthConfiguration;
import com.netatmo.auth.AuthManager;
import com.netatmo.auth.oauth.AuthManagerImpl;
import com.netatmo.auth.oauth.OAuthClient;
import com.netatmo.auth.oauth.OAuthResponse;
import com.netatmo.auth.token.RefreshTokenInterface;
import com.netatmo.auth.token.impl.PreferencesRefreshTokenImpl;
import com.netatmo.gcm.GCMProvider;
import com.netatmo.gcm.GCMUrlBuilder;
import com.netatmo.gcm.impl.GCMUrlBuilderImpl;
import com.netatmo.http.HttpClient;
import com.netatmo.http.impl.HttpClientImpl;
import com.netatmo.mbidon.pocwebrtc.api.GcmTestApi;
import com.netatmo.notification.provider.PushProvider;
import com.netatmo.storage.StorageManager;
import com.netatmo.storage.impl.StorageManagerImpl;
import com.netatmo.websocket.WebSocketFilter;
import com.netatmo.websocket.WebSocketParameters;
import com.netatmo.websocket.WebSocketProvider;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GCMDependenciesModule {

    @Provides
    @Singleton
    Context providesContext() {
        return MainApplication.get();
    }

    @Provides
    @Singleton
    GcmTestApi providesTestApi(HttpClient httpClient, GCMUrlBuilder gcmUrlBuilder) {
        return new GcmTestApi(httpClient, gcmUrlBuilder);
    }

    @Provides
    @Singleton
    AuthConfiguration providesAuthConfiguration(final RefreshTokenInterface refreshTokenInterface) {
        return new AuthConfiguration() {
            @NonNull
            @Override
            public String getAuthUrl() {
                return "https://api.inte.netatmo.com/oauth2/token";
            }

            @NonNull
            @Override
            public String getClientId() {
                return "na_client_android_welcome";
            }

            @NonNull
            @Override
            public String getClientSecret() {
                return "8ab584d62ca2a77e37ccc6b2c7e4f29e";
            }

            @NonNull
            @Override
            public String getPlatformKey() {
                return "";
            }

            @Nullable
            @Override
            public String getUserPrefix() {
                return null;
            }

            @Nullable
            @Override
            public RefreshTokenInterface getRefreshTokenInterface() {
                return refreshTokenInterface;
            }
        };
    }

    @Provides
    @Singleton
    ApplicationParameters providesApplicationParameters() {
        return new ApplicationParameters() {
            @NonNull
            @Override
            public String appVersionName() {
                return "gcm";
            }

            @Override
            public int appVersionCode() {
                return 0;
            }

            @NonNull
            @Override
            public String gcmSenderId() {
                return "749153456090";
            }

            @NonNull
            @Override
            public AppType appType() {
                return AppType.Camera;
            }
        };
    }

    @Provides
    @Singleton
    GCMUrlBuilder providesGcmUrlBuilder() {
        return new GCMUrlBuilderImpl("https://api.inte.netatmo.com/api");
    }

    @Provides
    AuthManager providesAuthManager(AuthClient<OAuthResponse> authClient, RefreshTokenInterface refreshTokenInterface) {
        return new AuthManagerImpl(authClient, refreshTokenInterface);
    }

    @Provides
    @Singleton
    HttpClient providesHttpClient() {
        return HttpClientImpl.builder().build();
    }

    @Provides
    @Singleton
    AuthClient<OAuthResponse> providesAuthClient(HttpClient httpClient,
                                                 AuthConfiguration authConfiguration,
                                                 ApplicationParameters appParamters) {
        return new OAuthClient(httpClient, authConfiguration, appParamters);
    }

    @Provides
    @Singleton
    RefreshTokenInterface provideRefreshToken(StorageManager storageManager) {
        return new PreferencesRefreshTokenImpl(storageManager);
    }

    @Provides
    @Singleton
    StorageManager providesStorageManager(Context applicationContext) {
        return new StorageManagerImpl(applicationContext);
    }

    @Provides
    @NonNull
    List<PushProvider> notificationProviders(@NonNull GCMProvider gcmProvider,
                                             @NonNull WebSocketProvider webSocketProvider) {
        List<PushProvider> providers = new LinkedList<>();
        providers.add(gcmProvider);
        providers.add(webSocketProvider);
        return providers;
    }

    @Provides
    @NonNull
    WebSocketParameters webSocketParameters() {
        return new WebSocketParameters() {
            @NonNull
            @Override
            public String url() {
                return "wss://my.inte.netatmo.com/ws/";
            }

            @NonNull
            @Override
            public WebSocketFilter filter() {
                return WebSocketFilter.All;
            }
        };
    }
}
