package com.music.di.component;

import android.app.Activity;
import android.content.Context;
import com.music.di.module.FragmentModule;
import com.music.di.scope.ContextLife;
import com.music.di.scope.PerFragment;
import dagger.Component;

/**
 */
@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {
    @ContextLife("Activity")
    Context getActivityContext();

    @ContextLife("Application")
    Context getApplicationContext();
    Activity getActivity();

}
