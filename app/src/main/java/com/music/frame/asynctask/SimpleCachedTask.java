package com.music.frame.asynctask;

import android.content.Context;
import android.os.Message;
import org.greenrobot.eventbus.EventBus;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * [description about this class]
 * 简单的异步任务，仅仅指定返回结果的类型，不可输入参数
 * @author jack
 */

public abstract class SimpleCachedTask<T extends Serializable> extends CachedTask<Object, Object, T> {

    private int taskId;
    private EventBus eventBus;

    public SimpleCachedTask(Context context, String key, long cacheTime, TimeUnit unit) {
        super(context, key, cacheTime, unit);
    }

    public SimpleCachedTask(Context context, int taskId, Object subscriber, String key, long cacheTime, TimeUnit unit) {
        this(context, taskId, subscriber, new EventBus(), key, cacheTime, unit);
    }

    public SimpleCachedTask(Context context, int taskId, Object subscriber, EventBus eventBus, String key, long cacheTime, TimeUnit unit) {
        super(context, key, cacheTime, unit);
        this.taskId = taskId;
        this.eventBus = eventBus;
        if(eventBus == null){
            this.eventBus = new EventBus();
        }
        this.eventBus.register(subscriber);
    }

    @Override
    protected T doConnectNetwork(Object... params) throws Exception {

        final T result = doConnectNetwork();
        if(eventBus != null){
            if(!isCancelled()){
                synchronized (this){
                    Message msg = Message.obtain();
                    msg.what = taskId;
                    msg.obj = result;
                    eventBus.post(msg);
                }
            }
        }
        return result;
    }

    protected abstract T doConnectNetwork() throws Exception;
}
