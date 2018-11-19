package com.music;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.music.di.component.ApplicationComponent;
import com.music.di.component.DaggerApplicationComponent;
import com.music.di.module.ApplicationModule;
import com.music.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * App application
 * @author jack
 */

public class AppDroid extends Application {

    private static AppDroid instance;
    private List<Activity> activities = new ArrayList<>();
    private UserInfo userInfo;
    private ApplicationComponent applicationComponent;

    /**
     * 初始化ApplicationComponent
     */
    private void initApplicationComponent() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }
    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //全局Application
        instance = this;
        initApplicationComponent();
        Fresco.initialize(this);
    }

    public static AppDroid getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 新建了一个activity
     * @param activity
     */
    public void addActivity(Activity activity){
        if(activities!=null && !activities.contains(activity)){
            activities.add(activity);
        }
    }

    /**
     *  结束指定的Activity
     * @param activity-
     */
    public void finishActivity(Activity activity){
        if (activity!=null) {
            this.activities.remove(activity);
            activity.finish();
        }
    }

    /**
     * 应用退出，结束所有的activity
     */
    public void exitClient(){
        for(Activity activity : activities) {
            if (activity!=null) {
                activity.finish();
            }
        }
        System.exit(0);
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}