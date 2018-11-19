package com.music.di.component;

import android.content.Context;
import com.music.di.module.ApplicationModule;
import com.music.di.scope.ContextLife;
import com.music.di.scope.PerApp;
import dagger.Component;

/**
 */
@PerApp
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    @ContextLife("Application")
    Context getApplication();
}