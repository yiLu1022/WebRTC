package com.netatmo.mbidon.pocwebrtc;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent()
public interface LoginActivityComponent extends AndroidInjector<LoginActivity> {
    /**
     * Provide Builder for {@link LoginActivity} so that this activity
     * can satisfy its dependencies without having knowledge of the root component.
     */
    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<LoginActivity> { }
}