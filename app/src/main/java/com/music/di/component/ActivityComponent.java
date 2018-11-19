package com.music.di.component;

import android.app.Activity;
import android.content.Context;
import com.music.di.module.ActivityModule;
import com.music.di.scope.ContextLife;
import com.music.di.scope.PerActivity;
import com.music.test.DaggerTestActivity;
import dagger.Component;

/**
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    @ContextLife("Activity")
    Context getActivityContext();

    @ContextLife("Application")
    Context getApplicationContext();
    Activity getActivity();

    void inject(DaggerTestActivity activity);

}
