package com.netatmo.mbidon.pocwebrtc.api;

import com.netatmo.api.BaseClient;
import com.netatmo.gcm.GCMUrlBuilder;
import com.netatmo.http.HttpClient;

public class GcmTestApi extends BaseClient {
    private final GCMUrlBuilder gcmUrlBuilder;

    /**
     * Public constructor
     *
     * @param httpClient the http client
     */
    public GcmTestApi(final HttpClient httpClient, final GCMUrlBuilder gcmUrlBuilder) {
        super(httpClient);
        this.gcmUrlBuilder = gcmUrlBuilder;
    }
}
