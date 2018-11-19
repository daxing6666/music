package com.music.di.component;

import android.content.Context;
import com.music.di.module.ServiceModule;
import com.music.di.scope.ContextLife;
import com.music.di.scope.PerService;
import dagger.Component;

/**
 */
@PerService
@Component(dependencies = ApplicationComponent.class, modules = ServiceModule.class)
public interface ServiceComponent {
    @ContextLife("Service")
    Context getServiceContext();

    @ContextLife("Application")
    Context getApplicationContext();
}
