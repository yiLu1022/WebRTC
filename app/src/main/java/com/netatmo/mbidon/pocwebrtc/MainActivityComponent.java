package com.netatmo.mbidon.pocwebrtc;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent()
public interface MainActivityComponent extends AndroidInjector<MainActivity> {
    /**
     * Provide Builder for {@link MainActivity} so that this activity
     * can satisfy its dependencies without having knowledge of the root component.
     */
    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<MainActivity> { }
}