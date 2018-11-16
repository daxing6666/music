package com.music.frame.asynctask;

import android.os.Message;
import org.greenrobot.eventbus.EventBus;

/**
 * [description about this class]
 * 简单的安全异步任务，仅仅指定返回结果的类型，不可输入参数
 * @author jack
 */

public abstract class SimpleSafeTask<T> extends SafeTask<Object, Object, T> {

    private int taskId;
    private EventBus eventBus;

    public SimpleSafeTask(){

    }

    public SimpleSafeTask(int taskId, Object subscriber){

        this(taskId, new EventBus(), subscriber);
    }

    public SimpleSafeTask(int taskId, EventBus eventBus, Object subscriber){

        this.taskId = taskId;
        this.eventBus = eventBus;
        if(eventBus == null){
            this.eventBus = new EventBus();
        }
        this.eventBus.register(subscriber);
    }

    @Override
    protected final T doInBackgroundSafely(Object... params) throws Exception {

        final T result = doInBackgroundSafely();
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

    protected abstract T doInBackgroundSafely() throws Exception;
}

