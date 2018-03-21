package com.netatmo.mbidon.pocwebrtc;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {LoginActivityComponent.class, MainActivityComponent.class})
public abstract class GCMAppModule {
    /**
     * Provide injector for {@link LoginActivity} so that this activity
     * can satisfy its dependencies without having knowledge of the root component.
     *
     * @param builder dagger builder.
     * @return dagger injector.
     */
    @Binds
    @IntoMap
    @ActivityKey(LoginActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindLoginActivityInjectorFactory(LoginActivityComponent.Builder builder);

    /**
     * Provide injector for {@link LoginActivity} so that this activity
     * can satisfy its dependencies without having knowledge of the root component.
     *
     * @param builder dagger builder.
     * @return dagger injector.
     */
    @Binds
    @IntoMap
    @ActivityKey(MainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindMainActivityInjectorFactory(MainActivityComponent.Builder builder);
}
