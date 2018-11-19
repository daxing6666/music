package com.music.di.module;

import android.content.Context;

import com.music.AppDroid;
import com.music.di.scope.ContextLife;
import com.music.di.scope.PerApp;
import dagger.Module;
import dagger.Provides;

/**
 */
@Module
public class ApplicationModule {
    private AppDroid mApplication;

    public ApplicationModule(AppDroid application) {
        mApplication = application;
    }

    @Provides
    @PerApp
    @ContextLife("Application")
    public Context provideApplicationContext() {
        return mApplication.getApplicationContext();
    }
}
